package homekit.communication

import encryption.ChaCha
import encryption.HKDF
import encryption.SRP
import homekit.Constants
import homekit.HomeKitServer
import homekit.communication.structure.data.Pairing
import utils.Logger
import java.io.InputStream
import java.net.Socket
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * Created by Mihael Valentin Berčič
 * on 05/02/2021 at 01:00
 * using IntelliJ IDEA
 */

class Session(private val socket: Socket, private val homeKitServer: HomeKitServer) {

    var isSecure = false
    var shouldClose = false
    val srp = SRP()
    var currentState: Int = 1
    private var controllerToAccessoryCount: Long = 0
    private var accessoryToControllerCount: Long = 0
    lateinit var currentController: Pairing

    lateinit var sessionKey: ByteArray
    private lateinit var sharedSecret: ByteArray
    private lateinit var accessoryToControllerKey: ByteArray
    private lateinit var controllerToAccessoryKey: ByteArray

    private val inputStream = socket.getInputStream()
    private val outputStream = socket.getOutputStream().buffered()

    init {
        try {
            socket.tcpNoDelay = true
            while (!shouldClose) {
                val aad = inputStream.readNBytes(2)
                if (aad.isEmpty()) {
                    close()
                    break
                }
                val shouldEncrypt = isSecure
                val request = if (!shouldEncrypt) readHeaders(inputStream, aad).let { HttpRequest(it, inputStream.readNBytes(it.contentLength)) }
                else {
                    val contentSize = ByteBuffer.allocate(2).order(ByteOrder.LITTLE_ENDIAN).put(aad).position(0).short.toInt()
                    val content = inputStream.readNBytes(contentSize)
                    val tag = inputStream.readNBytes(16)

                    val decodingBuffer = ByteBuffer.allocate(12 + contentSize + 16).apply {
                        order(ByteOrder.LITTLE_ENDIAN)
                        position(4)
                        putLong(controllerToAccessoryCount++)
                        put(content)
                        put(tag)
                    }

                    val decryptedContent = ChaCha.decrypt(decodingBuffer.array(), controllerToAccessoryKey, aad)
                    val headers = parseHeaders(decryptedContent)
                    HttpRequest(headers, decryptedContent.takeLast(headers.contentLength).toByteArray())
                }

                request.headers.apply {
                    Logger.trace("${Logger.green}${socket.remoteSocketAddress}${Logger.reset} [${Logger.red}$httpMethod${Logger.reset}] $path ${Logger.magenta}$query${Logger.reset} ${String(request.content)}")
                }
                homeKitServer.handle(request, this).apply { if (this == null) println("Null response for ${request.headers}") }?.apply { sendMessage(this, shouldEncrypt) }
                if (shouldClose) close()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            close()
        }
    }

    private fun close() {
        Logger.error("Input stream has let us know it is closed. Shutting this session down.")
        shouldClose = true
        homeKitServer.liveSessions.remove(this)
        socket.shutdownInput()
        socket.shutdownOutput()
        inputStream.close()
        outputStream.close()
        socket.close()
    }

    fun sendMessage(response: Response, encrypt: Boolean = true) {
        outputStream.write(if (!encrypt) response.data else encodeIntoFrames(response))
        outputStream.flush()
    }

    fun setSharedSecret(sharedSecret: ByteArray) {
        this.sharedSecret = sharedSecret
        homeKitServer.liveSessions.add(this)
        controllerToAccessoryCount = 0
        accessoryToControllerCount = 0
        Constants.apply {
            sessionKey = HKDF.compute("HMACSHA512", sharedSecret, verifyEncryptSalt, verifyEncryptInfo, 32)
            accessoryToControllerKey = HKDF.compute("HMACSHA512", sharedSecret, controlSalt, accessoryKeyInfo, 32)
            controllerToAccessoryKey = HKDF.compute("HMACSHA512", sharedSecret, controlSalt, controllerKeyInfo, 32)
        }
    }

    private fun encodeIntoFrames(response: Response): ByteArray {
        val dataToEncrypt = response.data.toList().chunked(1024)
        val totalSizeNeeded = dataToEncrypt.sumBy { it.size + 18 }
        val byteBuffer = ByteBuffer.allocate(totalSizeNeeded)

        dataToEncrypt.forEach { frame ->
            val dataLength = frame.size
            val littleEndianLength = ByteBuffer.allocate(2).order(ByteOrder.LITTLE_ENDIAN).putShort(dataLength.toShort()).array()
            val responseBuffer = ByteBuffer.allocate(12 + dataLength).apply {
                order(ByteOrder.LITTLE_ENDIAN)
                position(4)
                putLong(accessoryToControllerCount++)
                put(frame.toByteArray())
            }

            val encryptedResponse = ChaCha.encrypt(responseBuffer.array(), accessoryToControllerKey, littleEndianLength)
            val encodedResponseBuffer = ByteBuffer.allocate(2 + encryptedResponse.size).apply {
                order(ByteOrder.LITTLE_ENDIAN)
                put(littleEndianLength)
                put(encryptedResponse)
            }
            byteBuffer.put(encodedResponseBuffer.array())
        }
        return byteBuffer.array()
    }

    private fun readHeaders(inputStream: InputStream, missingBytes: ByteArray): HttpHeaders {
        val byteArray = missingBytes.toMutableList().apply {
            var headersFinished = false
            while (!headersFinished) {
                val index = lastIndex
                if (index >= 3) headersFinished = this[index - 3].compareTo(13) == 0
                        && this[index - 2].compareTo(10) == 0
                        && this[index - 1].compareTo(13) == 0
                        && this[index].compareTo(10) == 0
                if (!headersFinished) add(inputStream.read().toByte())
            }
        }.toByteArray()
        return parseHeaders(byteArray)
    }

    private fun parseHeaders(byteArray: ByteArray): HttpHeaders {
        val headers = String(byteArray)//.apply { Logger.info(this) }
        val infoMatcher = httpInfoPattern.matcher(headers).apply { find() }
        val lengthMatcher = contentLengthPattern.matcher(headers)
        val contentLength = if (lengthMatcher.find()) lengthMatcher.group("length").toInt() else 0
        val httpMethod = infoMatcher.group("method")
        val requestedPath = infoMatcher.group("path")
        if (httpMethod == null || requestedPath == null) throw Exception("Invalid http request.")
        val requestedMethod = HttpMethod.valueOf(httpMethod)
        val query = infoMatcher.group("query")?.replace("?id=", "")
        return HttpHeaders(requestedMethod, requestedPath, query, contentLength)
    }
}