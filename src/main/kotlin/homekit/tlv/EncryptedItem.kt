package homekit.tlv

import asHexString
import at.favre.lib.crypto.HKDF
import homekit.pairing.ChaCha
import homekit.pairing.srp.SRP
import homekit.tlv.structure.Item
import homekit.tlv.structure.TLVValue
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec


/**
 * Created by Mihael Valentin Berčič
 * on 24/12/2020 at 12:36
 * using IntelliJ IDEA
 */
class EncryptedItem(data: ByteArray) : Item {

    private val chaCha = ChaCha()

    fun thirdStep(srp: SRP) {
        val theirTag = data.takeLast(16).toByteArray()
        val cipherText = data.dropLast(16).toByteArray()

        val salt = "Pair-Setup-Controller-Sign-Salt".toByteArray()
        val info = "Pair-Setup-Controller-Sign-Info".toByteArray()

        val sharedSecret = srp.sharedSecret

        // Not used, cus produces 64 bytes...
        val mac = Mac.getInstance("HmacSHA512")
        mac.init(SecretKeySpec(sharedSecret, "HmacSHA512"))
        mac.update(salt)
        mac.update(info)
        val output = mac.doFinal().takeLast(32).toByteArray()


        println("Their tag:\n${theirTag.asHexString}\n-----------------------")
        println("My tag:\n${output.asHexString}")


        // Used from that library, no clue what next, because idk ...
        val hkdf = HKDF.fromHmacSha512()
        val pseudoRandomKey: ByteArray = hkdf.extract(salt, sharedSecret)
        val expandedIv = hkdf.expand(pseudoRandomKey, info, 32)
        val extract = hkdf.extract(salt, sharedSecret)


        val libOutput = hkdf.extractAndExpand(salt, sharedSecret, info, 32)

        println("Output: " + output.size)
        println("Cipher: " + cipherText.size)
        println("Total: " + data.size)
        chaCha.decrypt(cipherText, libOutput)
    }

    override val identifier: TLVValue = TLVValue.EncryptedData
    override val data: MutableList<Byte> = data.toMutableList()
    val items = mutableListOf<Item>()

}