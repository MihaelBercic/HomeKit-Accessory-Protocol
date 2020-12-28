package homekit.tlv.structure

import homekit.tlv.data.Value
import java.nio.ByteBuffer

/**
 * Created by Mihael Valentin Berčič
 * on 26/12/2020 at 01:19
 * using IntelliJ IDEA
 */
interface Item {

    val identifier: Value
    val dataLength: Int get() = data.size
    val data: MutableList<Byte>
    val needsFragmentation: Boolean get() = dataLength > 255

    val writeData: ByteBuffer.() -> Unit get() = { put(data.toByteArray()) }
    val writeFragmentedData: ByteBuffer.() -> Unit get() = {}
    val readData: ByteBuffer.() -> Unit get() = {}
}
