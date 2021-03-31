package encryption

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

    private const val algorithm = "ChaCha20-Poly1305"
    private const val nonceLength = 12

    @Synchronized
    fun encrypt(toEncode: ByteArray, key: ByteArray, aad: ByteArray = ByteArray(0)): ByteArray {
        val buffer = ByteBuffer.wrap(toEncode)
        val nonce = ByteArray(nonceLength)
        val data = ByteArray(buffer.remaining() - nonceLength)
        buffer[nonce][data]

        val cipher = Cipher.getInstance(algorithm).apply {
            init(Cipher.ENCRYPT_MODE, SecretKeySpec(key, "ChaCha20-Poly1305"), IvParameterSpec(nonce))
            if (aad.isNotEmpty()) updateAAD(aad)
        }
        return cipher.doFinal(data)
    }

    @Synchronized
    fun decrypt(encryptedData: ByteArray, key: ByteArray, aad: ByteArray = ByteArray(0)): ByteArray {
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
            if (aad.isNotEmpty()) it.updateAAD(aad)
            it.doFinal(encryptedText)
        }
    }

}