package homekit.tlv.structure

import java.nio.ByteBuffer
import kotlin.math.ceil

/**
 * Created by Mihael Valentin Berčič
 * on 26/12/2020 at 01:19
 * using IntelliJ IDEA
 */
open class TLVItem(val identifier: TLVValue, vararg content: Byte = ByteArray(0)) {

    val dataList = content.toMutableList()
    val dataArray get() = dataList.toByteArray()
    val dataLength: Int get() = dataList.size
    val totalLength: Int get() = dataLength + 2 * ceil(dataLength / 255.0).toInt()

    fun appendData(dataToAppend: ByteArray) = dataList.addAll(dataToAppend.toTypedArray())

    val writeData: ByteBuffer.() -> Unit
        get() = {
            val type = identifier.typeValue
            dataList.toList().chunked(255).forEach { fragment ->
                put(type)
                put(fragment.size.toByte())
                put(fragment.toByteArray())
            }
        }
    val readData: ByteBuffer.() -> Unit get() = {}


    override fun toString(): String = "$identifier [$dataLength]"
}
