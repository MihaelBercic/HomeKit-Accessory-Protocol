package homekit.tlv.structure

import java.nio.ByteBuffer
import kotlin.math.ceil

/**
 * Created by Mihael Valentin Berčič
 * on 26/12/2020 at 01:19
 * using IntelliJ IDEA
 */
interface Item {

    val identifier: TLVValue
    val dataLength: Int get() = data.size
    val data: MutableList<Byte>
    val totalLength: Int get() = dataLength + 2 * ceil(dataLength / 255.0).toInt()

    val writeData: ByteBuffer.() -> Unit
        get() = {
            val type = identifier.typeValue
            data.chunked(255).forEach { fragment ->
                put(type)
                put(fragment.size.toByte())
                put(fragment.toByteArray())
            }
        }
    val readData: ByteBuffer.() -> Unit get() = {}
}
