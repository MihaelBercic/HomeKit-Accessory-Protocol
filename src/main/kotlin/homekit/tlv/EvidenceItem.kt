package homekit.tlv

import homekit.tlv.structure.TLVValue
import homekit.tlv.structure.Item

/**
 * Created by Mihael Valentin Berčič
 * on 28/12/2020 at 17:46
 * using IntelliJ IDEA
 */
class EvidenceItem(proof: ByteArray) : Item {

    override val identifier: TLVValue = TLVValue.Proof
    override val data: MutableList<Byte> = proof.toMutableList()

}