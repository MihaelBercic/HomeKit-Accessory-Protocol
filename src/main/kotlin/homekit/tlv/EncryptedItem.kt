package homekit.tlv

import asByteArray
import asHexString
import homekit.pairing.srp.SRP
import homekit.tlv.structure.Item
import homekit.tlv.structure.TLVValue
import net.i2p.crypto.eddsa.spec.EdDSAPrivateKeySpec
import javax.crypto.Cipher
import javax.crypto.Mac
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * Created by Mihael Valentin Berčič
 * on 24/12/2020 at 12:36
 * using IntelliJ IDEA
 */
class EncryptedItem(data: ByteArray) : Item {


    fun thirdStep(srp: SRP) {
        val salt = "Pair-Setup-Controller-Sign-Salt".toByteArray()
        val info = "Pair-Setup-Controller-Sign-Info".toByteArray()
        val sharedSecret = srp.sessionKey.asByteArray
        val hkdf = Mac.getInstance("HmacSHA512")



    }

    val items = mutableListOf<Item>()
    override val identifier: TLVValue = TLVValue.EncryptedData
    override val data: MutableList<Byte> = data.dropLast(16).toMutableList()

}