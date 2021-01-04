package homekit.tlv

import homekit.tlv.structure.PairingMethod
import homekit.tlv.structure.TLVValue
import homekit.tlv.structure.Item

/**
 * Created by Mihael Valentin Berčič
 * on 24/12/2020 at 12:36
 * using IntelliJ IDEA
 */
class MethodItem(method: PairingMethod) : Item {

    override val identifier: TLVValue = TLVValue.Method
    override val data: MutableList<Byte> = mutableListOf(method.typeValue)

}