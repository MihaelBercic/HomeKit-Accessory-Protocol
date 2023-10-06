package homekit.encryption

import java.nio.ByteBuffer
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import kotlin.math.ceil

/**
 * Created by Mihael Valentin Berčič
 * on 13/01/2021 at 16:25
 * using IntelliJ IDEA
 */
object HKDF {

    private fun expand(algorithm: String, pseudoRandomKey: ByteArray, info: ByteArray, hashLength: Int, outputLength: Int): ByteArray {
        val infoLength = info.size
        val amount = ceil(outputLength.toDouble() / hashLength).toInt()
        val mac = macWithKey(algorithm, pseudoRandomKey)

        val computations = mutableListOf(ByteArray(0))
        (0..amount).forEachIndexed { index, _ ->
            val previous = computations[index]
            val previousSize = previous.size
            val concatenated = ByteBuffer.allocate(previousSize + infoLength + 1).apply {
                put(previous)
                put(info)
                put((index + 1).toByte())
            }
            val hashed = mac.doFinal(concatenated.array())
            computations.add(hashed)
            mac.reset()
        }
        return computations.map { it.toList() }.flatten().take(outputLength).toByteArray()
    }

    private fun extract(algorithm: String, salt: ByteArray, input: ByteArray): ByteArray = macWithKey(algorithm, salt).doFinal(input)

    fun compute(algorithm: String, inputKeyingMaterial: ByteArray, salt: ByteArray, info: ByteArray, outputLength: Int): ByteArray {
        val hashLength = Mac.getInstance(algorithm).macLength
        val pseudoRandomKey = extract(algorithm, salt, inputKeyingMaterial)
        return expand(algorithm, pseudoRandomKey, info, hashLength, outputLength)
    }

    private fun macWithKey(algorithm: String, key: ByteArray) = Mac.getInstance(algorithm).apply { init(SecretKeySpec(key, algorithm)) }
}