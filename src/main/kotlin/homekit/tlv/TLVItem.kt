package homekit.tlv

import java.nio.ByteBuffer
import kotlin.math.ceil

/**
 * Created by Mihael Valentin Berčič
 * on 26/12/2020 at 01:19
 * using IntelliJ IDEA
 */

/**
 * This class allows the creation of TLV8 Item that is used by Apple HAP Protocol.
 *
 * A TLV8 data structure is a type-length-value (TLV) item that has an 8-bit type, 8-bit length, and N-byte value.
 *
 * @property identifier Specific Tag value as specified by HAP documentation. Check out
 * @param content TLV Item's content as an array of bytes. Used when creating a TLVItem that is already populated.
 * @see [Tag] for further value information.
 */
open class TLVItem(val identifier: Tag, vararg content: Byte = ByteArray(0)) {

    val dataList = content.toMutableList()
    val dataArray get() = dataList.toByteArray()
    val totalLength: Int get() = dataLength + 2 * ceil(dataLength / 255.0).toInt()
    private val dataLength: Int get() = dataList.size

    /**
     * Appends the additional data to the existing content.
     *
     * In HAP specification it is used due to max Item length and fragmentation.
     *
     * @param dataToAppend
     */
    fun appendData(dataToAppend: ByteArray) = dataList.addAll(dataToAppend.toTypedArray())

    /**
     * Writes the content of the [TLVItem] in order Tag-Length-Value to the passed buffer.
     *
     * @param buffer Buffer to write the [TLVItem] to.
     */
    open fun writeData(buffer: ByteBuffer) {
        val type = identifier.typeValue.toByte()
        buffer.apply {
            dataList.toList().chunked(255).forEach { fragment ->
                put(type)
                put(fragment.size.toByte())
                put(fragment.toByteArray())
            }
        }
    }

    override fun toString(): String = "$identifier [$dataLength]"
}
