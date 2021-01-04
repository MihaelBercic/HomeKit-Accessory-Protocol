package homekit.tlv

import homekit.tlv.structure.TLVValue
import homekit.tlv.structure.Item

/**
 * Created by Mihael Valentin Berčič
 * on 24/12/2020 at 12:36
 * using IntelliJ IDEA
 */
class IdentifierItem(tag: String) : Item {

    override val identifier: TLVValue = TLVValue.Identifier
    override val data: MutableList<Byte> = tag.toByteArray().toMutableList()

}