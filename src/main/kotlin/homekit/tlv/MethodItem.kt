package homekit.tlv

import homekit.tlv.data.Method
import homekit.tlv.data.Value
import homekit.tlv.structure.Item

/**
 * Created by Mihael Valentin Berčič
 * on 24/12/2020 at 12:36
 * using IntelliJ IDEA
 */
class MethodItem(method: Method) : Item {

    override val identifier: Value = Value.Method
    override val data: MutableList<Byte> = mutableListOf(method.typeValue)

}