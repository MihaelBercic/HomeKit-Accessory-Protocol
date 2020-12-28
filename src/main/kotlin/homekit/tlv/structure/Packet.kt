package homekit.tlv.structure

import homekit.tlv.MethodItem
import homekit.tlv.StateItem
import homekit.tlv.data.Method
import homekit.tlv.data.Value
import java.nio.ByteBuffer

/**
 * Created by Mihael Valentin Berčič
 * on 26/12/2020 at 01:20
 * using IntelliJ IDEA
 */
class Packet {

    val items: MutableList<Item> = mutableListOf()

    fun find(predicate: (Item) -> Boolean) = items.firstOrNull(predicate)

    constructor(vararg items: Item) {
        this.items.addAll(items)
    }

    constructor(byteArray: ByteArray) {
        val byteBuffer = ByteBuffer.wrap(byteArray)
        var previous: Item? = null
        loop@ while (byteBuffer.hasRemaining()) {
            val type = Value.valueOf(byteBuffer.get())
            val length = byteBuffer.get().toInt() and 255
            val dataArray = ByteArray(length)
            byteBuffer.get(dataArray)

            if (previous?.identifier == type) {
                previous.data.addAll(dataArray.toList())
                continue@loop
            }

            val item: Item = when (type) {
                Value.State -> StateItem(dataArray[0])
                Value.Method -> MethodItem(Method.valueOf(dataArray[0]))
                else -> {
                    println("T: $type ($length)")
                    continue@loop
                }
            }
            items.add(0, item)
            previous = item
        }
    }

    fun toByteArray() = ByteArray(5000).apply {
        val buffer = ByteBuffer.wrap(this)
        items.forEach {
            if (it.needsFragmentation) buffer.apply(it.writeFragmentedData)
            else {
                buffer.put(it.identifier.typeValue)
                buffer.put(it.dataLength.toByte())
                buffer.apply(it.writeData)
            }
        }
    }

}