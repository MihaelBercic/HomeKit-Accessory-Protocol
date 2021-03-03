package homekit.tlv.structure

import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * Created by Mihael Valentin Berčič
 * on 26/12/2020 at 01:20
 * using IntelliJ IDEA
 */
class TLVPacket {

    val items: MutableList<TLVItem> = mutableListOf()

    operator fun get(tlvValue: TLVValue) = items.firstOrNull { it.identifier == tlvValue } ?: throw Exception("No $tlvValue in this packet.")

    constructor(vararg items: TLVItem) {
        this.items.addAll(items)
    }

    constructor(byteArray: ByteArray) {
        items.addAll(parseTLV(byteArray))
    }

    fun toByteArray(): ByteArray = ByteBuffer.allocate(items.sumBy { it.totalLength }).apply { items.forEach { it.writeData(this) } }.array()


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
        if (byteBuffer.remaining() != 0) throw java.lang.Exception("Byte buffer has something more to read!")
        return items
    }
}