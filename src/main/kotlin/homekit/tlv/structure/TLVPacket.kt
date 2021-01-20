package homekit.tlv.structure

import homekit.tlv.parseTLV
import java.nio.ByteBuffer

/**
 * Created by Mihael Valentin Berčič
 * on 26/12/2020 at 01:20
 * using IntelliJ IDEA
 */
class TLVPacket {

    private val items: MutableList<TLVItem> = mutableListOf()

    operator fun get(tlvValue: TLVValue) = items.firstOrNull { it.identifier == tlvValue } ?: throw Exception("No $tlvValue in this packet.")

    constructor(vararg items: TLVItem) {
        this.items.addAll(items)
    }

    constructor(byteArray: ByteArray) {
        items.addAll(parseTLV(byteArray))
    }

    fun toByteArray(): ByteArray = ByteBuffer.allocate(items.sumBy { it.totalLength }).apply { items.forEach { it.writeData(this) } }.array()

}