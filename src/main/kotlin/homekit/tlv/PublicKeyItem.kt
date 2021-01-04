package homekit.tlv

import homekit.tlv.structure.TLVValue
import homekit.tlv.structure.Item

/**
 * Created by Mihael Valentin Berčič
 * on 24/12/2020 at 12:36
 * using IntelliJ IDEA
 */
class PublicKeyItem(publicKey: ByteArray) : Item {

    override val identifier: TLVValue = TLVValue.PublicKey
    override val data: MutableList<Byte> = publicKey.toMutableList().apply { println("Public key has size: $size") }

}