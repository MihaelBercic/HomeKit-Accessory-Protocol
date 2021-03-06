package homekit.tlv

import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * Created by Mihael Valentin Berčič
 * on 26/12/2020 at 01:20
 * using IntelliJ IDEA
 *
 * This class is used to store multiple [TLVItem] in a single packet.
 *
 * The class conforms to the specifications in HAP-Specification released by Apple.
 */
class TLVPacket {

    private val items: MutableList<TLVItem> = mutableListOf()

    constructor(vararg items: TLVItem) {
        this.items.addAll(items)
    }

    constructor(byteArray: ByteArray) {
        items.addAll(parseTLV(byteArray))
    }

    val asByteArray: ByteArray get() = ByteBuffer.allocate(items.sumBy { it.totalLength }).apply { items.forEach { it.writeData(this) } }.array()

    operator fun get(tlvValue: TLVValue) = items.firstOrNull { it.identifier == tlvValue } ?: throw Exception("No $tlvValue in this packet.")


    private fun parseTLV(byteArray: ByteArray): List<TLVItem> {
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