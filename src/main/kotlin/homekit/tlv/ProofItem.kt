package homekit.tlv

import homekit.tlv.data.Value
import homekit.tlv.structure.Item

/**
 * Created by Mihael Valentin Berčič
 * on 28/12/2020 at 17:46
 * using IntelliJ IDEA
 */
class ProofItem(proof: ByteArray) : Item {

    override val identifier: Value = Value.Proof
    override val data: MutableList<Byte> = proof.toMutableList()

}