package homekit.tlv.structure

import homekit.tlv.parseTLV
import java.nio.ByteBuffer

/**
 * Created by Mihael Valentin Berčič
 * on 26/12/2020 at 01:20
 * using IntelliJ IDEA
 */
class Packet {

    private val items: MutableList<Item> = mutableListOf()

    fun find(predicate: (Item) -> Boolean) = items.firstOrNull(predicate)

    inline fun <reified T : Item> get(noinline predicate: (Item) -> Boolean = { it is T }) = find(predicate) as? T ?: throw Exception("TLV item ${T::class.java} not found")

    constructor(vararg items: Item) {
        this.items.addAll(items)
    }

    constructor(byteArray: ByteArray) {
        items.addAll(parseTLV(byteArray))
    }

    fun toByteArray(): ByteArray = ByteBuffer.allocate(items.sumBy { it.totalLength }).apply { items.forEach { it.writeData(this) } }.array()

}