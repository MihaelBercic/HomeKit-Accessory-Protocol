package homekit.tlv

import homekit.tlv.structure.TLVItem
import homekit.tlv.structure.TLVValue
import java.lang.Exception
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * Created by Mihael Valentin Berčič
 * on 03/01/2021 at 00:36
 * using IntelliJ IDEA
 */

// TODO find more appropriate place for this. Also name.

fun parseTLV(byteArray: ByteArray): List<TLVItem> {
    val items = mutableListOf<TLVItem>()
    val byteBuffer = ByteBuffer.wrap(byteArray).order(ByteOrder.LITTLE_ENDIAN)
    var previous: TLVItem? = null
    loop@ while (byteBuffer.hasRemaining()) {
        val type = TLVValue.valueOf(byteBuffer.get())
        val length = byteBuffer.get().toInt() and 255
        val dataArray = ByteArray(length)
        byteBuffer[dataArray]

        if (previous?.identifier == type) previous.appendData(dataArray)
        else TLVItem(type, *dataArray).apply {
            items.add(0, this)
            previous = this
        }
    }
    if(byteBuffer.remaining() != 0) throw Exception("Byte buffer has something more to read!")
    return items
}