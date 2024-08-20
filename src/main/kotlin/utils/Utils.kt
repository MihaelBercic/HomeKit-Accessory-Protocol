package utils

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import mdns.packet.PacketReader
import java.io.File
import java.math.BigInteger
import java.net.DatagramPacket
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest.BodyPublishers
import java.net.http.HttpResponse
import java.util.*


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


val String.asBigInteger get() = BigInteger(this, 16)
val ByteArray.asBigInteger get() = BigInteger(1, this)


infix fun BigInteger.padded(length: Int): ByteArray {
    val array = asByteArray
    val difference = length - array.size

    return if (difference <= 0) array
    else array.copyInto(ByteArray(length), destinationOffset = difference)
}

val BigInteger.asByteArray: ByteArray
    get() = toByteArray().let {
        assert(signum() != -1)
        if (it[0].toInt().and(255) == 0) it.copyOfRange(1, it.size) else it
    }

inline fun <reified T> readOrCompute(name: String, block: () -> T): T = File(name).let {
    if (it.exists()) gson.fromJson(it.readText(), T::class.java)
    else block.invoke()
}

val httpClient = HttpClient.newHttpClient()

fun urlRequest(type: HttpMethod, url: String, body: Any, callback: (code: Int, response: String) -> Unit = { _, _ -> }) {
    try {
        val request = java.net.http.HttpRequest.newBuilder(URI(url)).apply {
            when (type) {
                HttpMethod.GET -> GET()
                HttpMethod.POST -> POST(BodyPublishers.ofString(body.toString()))
                HttpMethod.PUT -> PUT(BodyPublishers.ofString(body.toString()))
                HttpMethod.DELETE -> DELETE()
                else -> {}
            }
        }.build()
        val response = httpClient.send(request, HttpResponse.BodyHandlers.ofString())
        callback(response.statusCode(), response.body())
    } catch (e: Exception) {
        Logger.error("URL error to ${type.name} $url")
        e.printStackTrace()
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