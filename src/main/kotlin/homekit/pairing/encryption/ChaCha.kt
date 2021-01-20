package homekit.pairing.encryption

import java.nio.ByteBuffer
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * Created by Mihael Valentin Berčič
 * on 13/01/2021 at 19:59
 * using IntelliJ IDEA
 */


object ChaCha {

    private val algorithm = "ChaCha20-Poly1305"
    private val nonceLength = 12

    fun encrypt(data: ByteArray, key: ByteArray, nonce: String): ByteArray {
        val buffer = ByteBuffer.allocate(nonceLength).apply {
            position(4)
            put(nonce.toByteArray())
        }
        val paddedNonce = buffer.array()
        val iv = IvParameterSpec(paddedNonce)
        val secretKey = SecretKeySpec(key, "ChaCha20-Poly1305")
        val cipher = Cipher.getInstance(algorithm)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, iv)


        val encryptedText = cipher.doFinal(data)

        // TODO can't possibly be correct. Has to be (nonce || actual_ciphertext || tag)
        return encryptedText
    }

    fun decrypt(encryptedData: ByteArray, key: ByteArray): ByteArray {
        val secretKey = SecretKeySpec(key, algorithm)
        val encryptedText = ByteArray(encryptedData.size - nonceLength)
        val nonce = ByteArray(nonceLength)

        ByteBuffer.wrap(encryptedData).apply {
            this[nonce]
            this[encryptedText]
        }

        val iv = IvParameterSpec(nonce)
        return Cipher.getInstance(algorithm).let {
            it.init(Cipher.DECRYPT_MODE, secretKey, iv)
            it.doFinal(encryptedText)
        }
    }

}