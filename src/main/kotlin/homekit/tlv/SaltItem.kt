package homekit.tlv

import homekit.tlv.structure.Item
import homekit.tlv.structure.TLVValue

/**
 * Created by Mihael Valentin Berčič
 * on 24/12/2020 at 12:36
 * using IntelliJ IDEA
 */
class SaltItem(salt: ByteArray) : Item {

    override val identifier: TLVValue = TLVValue.Salt
    override val data: MutableList<Byte> = salt.toMutableList()

}