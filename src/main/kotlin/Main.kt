import at.favre.lib.crypto.HKDF
import homekit.HomeKitServer
import homekit.HomeKitService
import java.io.File
import java.nio.ByteBuffer
import java.util.*
import javax.crypto.Cipher
import javax.crypto.Mac
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * Created by Mihael Valentin Berčič
 * on 08/12/2020 at 22:41
 * using IntelliJ IDEA
 */

val salt = "Pair-Setup-Controller-Sign-Salt".toByteArray()
val info = "Pair-Setup-Controller-Sign-Info".toByteArray()
val outputLength = 32

// Stored from Pair Setup
val data = File("data").readBytes()
val key = File("secret").readBytes()
val session = File("session").readBytes()

fun main() {
    val hkdf = HKDF.fromHmacSha512() // Using HKDF implementation, but was wondering if there is a way of doing so without an external lib...
    val saltExtract = hkdf.extract(salt, key)
    val infoExpand = hkdf.expand(key, info, 32)
    val outputHKDF = hkdf.extractAndExpand(saltExtract, key, infoExpand, outputLength)

    Base64.getEncoder().apply {
        println("Key: " + encodeToString(key))
        println("Session: " + encodeToString(session))
        println("Salt: " + encodeToString(salt))
        println("Info: " + encodeToString(info))
    }
    println("OutputHKDF size: " + outputHKDF.size)


    // Padding the 12 byte nonce with 0 bytes (Not even sure if this is correct or not) (Went into Apple ADK and read from their code they do padding)
    val nonceBuffer = ByteBuffer.allocate(12).apply {
        put(4, "PS-Msg05".toByteArray())
    }
    val nonce = nonceBuffer.array()
    val keyFromOutput = SecretKeySpec(outputHKDF, "ChaCha20-Poly1305")
    val nonceSpec = IvParameterSpec(nonce)
    val cipher = Cipher.getInstance("ChaCha20-Poly1305").apply {
        init(Cipher.DECRYPT_MODE, keyFromOutput, nonceSpec)
    }

    val decoded = cipher.doFinal(data) // Produces Tag mismatch error


    // Testing of HMAC SHA 512
    val mac = Mac.getInstance("HmacSHA512").apply {
        init(SecretKeySpec(key, "HmacSHA512"))
        update(salt)
        update(info)
    }
    val output = mac.doFinal()

    return
    HomeKitService().startAdvertising(30000)
    HomeKitServer()
}