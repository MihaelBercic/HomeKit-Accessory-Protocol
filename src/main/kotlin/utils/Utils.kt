package utils

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import mdns.packet.PacketReader
import java.io.File
import java.math.BigInteger
import java.net.DatagramPacket
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest
import java.util.*
import kotlin.experimental.xor
import kotlin.math.pow


/**
 * Created by Mihael Valentin Berčič
 * on 19/12/2020 at 22:52
 * using IntelliJ IDEA
 */

// TODO cleanup... pls...

fun BitSet.minimumBytes(n: Int): ByteArray {
    val byteArray = toByteArray()
    val difference = n - byteArray.size
    if (difference == 0) return byteArray.reversedArray()
    return byteArray.toMutableList().apply { addAll(ByteArray(difference).toTypedArray()) }.toByteArray()
        .reversedArray()
}

fun generateMAC(): String {
    val random = Random()
    return (0 until 6).joinToString(":") { Integer.toHexString(random.nextInt(255) + 1).padStart(2, '0') }.toUpperCase()
}

fun Int.bits(from: Int, count: Int): Int = (this shr from) and (2.0.pow(count) - 1).toInt()


// To be ignored. Is code simply for debugging
val ByteArray.asBinaryString
    get() = "\t" + map { it.asString.padStart(8, '0') }.chunked(4).joinToString("\n\t") { it.joinToString(" ") }

val ByteArray.asHexString
    get() = toList()
        .chunked(4)
        .map {
            it.joinToString("") { Integer.toHexString(it.toInt() and 255).padStart(2, '0') }
        }
        .chunked(4)
        .joinToString("\n") { it.joinToString(" ") }


val Byte.asString get() = Integer.toBinaryString(toInt() and 255)
val Byte.asHexString get() = Integer.toHexString(toInt())

@Synchronized
fun MessageDigest.hash(vararg bytes: Byte): ByteArray {
    update(bytes)
    return digest()
}

infix fun ByteArray.xor(b2: ByteArray): ByteArray {
    val result = ByteArray(size)
    for (i in indices) result[i] = (this[i] xor b2[i])
    return result
}


val String.asBigInteger get() = BigInteger(this, 16)
val ByteArray.asBigInteger get() = BigInteger(1, this)


infix fun BigInteger.padded(length: Int): ByteArray {
    val array = asByteArray
    val difference = length - array.size

    return if (difference <= 0) array
    else array.copyInto(ByteArray(length), destinationOffset = difference)
}

val BigInteger.asHex get() = asByteArray.asHexString
val BigInteger.asByteArray: ByteArray
    get() = toByteArray().let {
        assert(signum() != -1)
        if (it[0].toInt().and(255) == 0) it.copyOfRange(1, it.size) else it
    }

inline fun <reified T> readOrCompute(name: String, block: () -> T) = File(name).let {
    if (it.exists()) gson.fromJson(it.readText(), T::class.java)
    else block.invoke()
}

enum class NetworkRequestType {
    GET, POST, PUT
}


fun urlRequest(type: NetworkRequestType, url: String, body: Any, callback: (code: Int, response: String) -> Unit = { _, _ -> }) {
    val connection = (URL(url).openConnection() as HttpURLConnection)
    try {
        connection.requestMethod = type.name
        connection.doOutput = true
        connection.doInput = true
        connection.connect()
        val outputStream = connection.outputStream
        val inputStream = connection.inputStream
        when (body) {
            is String -> if (body.isNotEmpty()) outputStream.write(body.toByteArray())
            // Left room for other types
        }
        val response = connection.inputStream.readAllBytes()

        outputStream.close()
        inputStream.close()
        connection.disconnect()
        callback(connection.responseCode, String(response))
    } catch (e: Exception) {
        Logger.error("URL error to $url")
        e.printStackTrace()
    } finally {
        connection.disconnect()
    }
}

/**
 * Created by Mihael Valentin Berčič
 * on 08/12/2020 at 22:41
 * using IntelliJ IDEA
 */
val gson: Gson = GsonBuilder().create()
val appleGson: Gson = GsonBuilder().excludeFieldsWithoutExposeAnnotation().create()
val DatagramPacket.asPacket get() = PacketReader(this).buildPacket()

inline fun <reified T> Map<String, Any>.require(key: String, message: () -> String): T {
    val value = this[key] ?: throw Exception(message())
    return when (T::class) {
        Int::class -> (value as Double).toInt() as T
        else -> value as T
    }
}