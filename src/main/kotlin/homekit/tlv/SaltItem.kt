package homekit.tlv

import homekit.tlv.data.Value
import homekit.tlv.structure.Item

/**
 * Created by Mihael Valentin Berčič
 * on 24/12/2020 at 12:36
 * using IntelliJ IDEA
 */
class SaltItem(salt: ByteArray) : Item {

    override val identifier: Value = Value.Salt
    override val data: MutableList<Byte> = salt.toMutableList() // Big endian to little endian

}