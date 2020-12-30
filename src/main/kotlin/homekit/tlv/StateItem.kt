package homekit.tlv

import homekit.tlv.data.Value
import homekit.tlv.structure.Item

/**
 * Created by Mihael Valentin Berčič
 * on 24/12/2020 at 12:36
 * using IntelliJ IDEA
 */
class StateItem(val value: Byte) : Item {

    override val data: MutableList<Byte> = mutableListOf(value)
    override val identifier: Value = Value.State

}