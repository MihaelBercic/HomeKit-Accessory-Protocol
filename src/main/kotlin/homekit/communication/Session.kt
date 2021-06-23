package homekit.communication

import homekit.HomeKitServer
import homekit.communication.LiveSessions.removeFromLiveSessions
import homekit.communication.LiveSessions.secureSessionStarted
import homekit.encryption.ChaCha
import homekit.encryption.HKDF
import homekit.encryption.SRP
import homekit.structure.data.Pairing
import utils.Constants
import utils.HttpMethod
import utils.Logger
import java.io.InputStream
import java.net.Socket
import java.net.SocketAddress
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * Created by Mihael Valentin Berčič
 * on 05/02/2021 at 01:00
 * using IntelliJ IDEA
 */
class Session(private val socket: Socket, homeKitServer: HomeKitServer) {

    var isSecure = false
    var shouldClose = false
    val srp = SRP()
    var currentState: Int = 1
    val remoteSocketAddress: SocketAddress = socket.remoteSocketAddress

    private var controllerToAccessoryCount: Long = 0
    private var accessoryToControllerCount: Long = 0
    lateinit var currentController: Pairing

    lateinit var sessionKey: ByteArray
    private lateinit var sharedSecret: ByteArray
    private lateinit var accessoryToControllerKey: ByteArray
    private lateinit var controllerToAccessoryKey: ByteArray

    private val inputStream = socket.getInputStream()
    private val outputStream = socket.getOutputStream()

    private val contentBuffer = ByteBuffer.allocate(100000)

    init {
        Logger.info("New connection from $remoteSocketAddress")
        try {
            while (!shouldClose) {
                val aad = inputStream.readNBytes(2)
                if (aad.isEmpty()) {
                    Logger.error("Input stream has sent EOF! [$remoteSocketAddress]")
                    break
                }
                val shouldEncrypt = isSecure
                if (!shouldEncrypt) {
                    val headers = readHeaders(inputStream, aad)
                    val request = HttpRequest(headers, inputStream.readNBytes(headers.contentLength))
                    val response = homeKitServer.handle(request, this)
                    sendMessage(response, shouldEncrypt)
                } else {
                    val bytes = aad.map { it.toInt() and 0xFF }
                    val contentSize = (bytes[1] shl 8) or bytes[0]

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
                    contentBuffer.put(decryptedContent)
                    println(contentSize)
                    if (contentSize < 1024) {
                        val data = ByteArray(contentBuffer.position())
                        contentBuffer.get(0, data)
                        val headers = parseHeaders(data)
                        val request = HttpRequest(headers, data.takeLast(headers.contentLength).toByteArray())
                        val response = homeKitServer.handle(request, this)
                        contentBuffer.position(0)
                        sendMessage(response, shouldEncrypt)
                    }

                }
            }
            Logger.error("Should close has been set to $shouldClose!")
            close()
        } catch (e: Exception) {
            e.printStackTrace()
            close()
        }
    }

    private fun close() {
        Logger.error("Shutting this session down. [${socket.remoteSocketAddress}]")
        socket.shutdownInput()
        socket.shutdownOutput()
        inputStream.close()
        outputStream.close()
        socket.close()
        removeFromLiveSessions()
    }

    fun sendMessage(response: Response, encrypt: Boolean = true) {
        outputStream.write(if (!encrypt) response.data else encodeIntoFrames(response))
        outputStream.flush()
    }

    fun setSharedSecret(sharedSecret: ByteArray) {
        this.sharedSecret = sharedSecret
        controllerToAccessoryCount = 0
        accessoryToControllerCount = 0
        Constants.apply {
            sessionKey = HKDF.compute("HMACSHA512", sharedSecret, verifyEncryptSalt, verifyEncryptInfo, 32)
            accessoryToControllerKey = HKDF.compute("HMACSHA512", sharedSecret, controlSalt, accessoryKeyInfo, 32)
            controllerToAccessoryKey = HKDF.compute("HMACSHA512", sharedSecret, controlSalt, controllerKeyInfo, 32)
        }
        secureSessionStarted()
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
        val content = String(byteArray)
        val infoMatcher = httpInfoPattern.matcher(content).apply { find() }
        val lengthMatcher = contentLengthPattern.matcher(content)
        val contentLength = if (lengthMatcher.find()) lengthMatcher.group("length").toInt() else 0
        val httpMethod = infoMatcher.group("method")
        val requestedPath = infoMatcher.group("path")

        if (httpMethod == null || requestedPath == null) throw Exception("Invalid http request.")
        val requestedMethod = HttpMethod.valueOf(httpMethod)
        val query = infoMatcher.group("query")?.drop(1)
        return HttpHeaders(requestedMethod, requestedPath, query, contentLength)
    }
}