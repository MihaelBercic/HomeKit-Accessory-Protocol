package homekit.tlv

import homekit.tlv.data.Value
import homekit.tlv.structure.Item
import java.nio.ByteBuffer

/**
 * Created by Mihael Valentin Berčič
 * on 24/12/2020 at 12:36
 * using IntelliJ IDEA
 */
class PublicKeyItem(publicKey: ByteArray) : Item {


    override val identifier: Value = Value.PublicKey
    override val data: MutableList<Byte> = publicKey.reversedArray().toMutableList()

    override val writeFragmentedData: ByteBuffer.() -> Unit = {
        data.toList().chunked(255).forEach {
            val actualSize = it.size.toByte()
            put(identifier.typeValue)
            put(actualSize)
            put(it.toByteArray())
        }
    }
}