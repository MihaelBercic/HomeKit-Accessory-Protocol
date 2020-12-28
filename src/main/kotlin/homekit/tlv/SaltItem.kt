package homekit.tlv

import homekit.tlv.data.Value
import homekit.tlv.structure.Item
import java.math.BigInteger

/**
 * Created by Mihael Valentin Berčič
 * on 24/12/2020 at 12:36
 * using IntelliJ IDEA
 */
class SaltItem(salt: BigInteger) : Item {

    override val identifier: Value = Value.Salt
    override val data: MutableList<Byte> = salt.toByteArray().reversedArray().toMutableList() // Big endian to little endian

}