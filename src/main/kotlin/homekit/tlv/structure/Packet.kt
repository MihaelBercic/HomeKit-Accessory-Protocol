package homekit.tlv.structure

import asHexString
import homekit.tlv.MethodItem
import homekit.tlv.ProofItem
import homekit.tlv.PublicKeyItem
import homekit.tlv.StateItem
import homekit.tlv.data.Method
import homekit.tlv.data.Value
import java.math.BigInteger
import java.nio.ByteBuffer
import java.nio.ByteOrder

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
                println("Length before adding fragment: ${previous.dataLength}")
                previous.data.addAll(dataArray.toList())
                previous.data.toByteArray().apply { println("---------------------PublicKey---------------------\n${asHexString}\n---------------------PublicKey---------------------") }
                println("Added fragmented data! Total size: ${previous.dataLength}")
            } else {
                val item: Item = when (type) {
                    Value.State -> StateItem(dataArray[0])
                    Value.Method -> MethodItem(Method.valueOf(dataArray[0]))
                    Value.PublicKey -> PublicKeyItem(dataArray)
                    Value.Proof -> ProofItem(dataArray).apply { println("---------------------Proof---------------------\n${dataArray.asHexString}\n---------------------Proof---------------------") }
                    else -> {
                        println("T: $type ($length)")
                        continue@loop
                    }
                }
                items.add(0, item)
                previous = item
            }
        }
    }

    fun toByteArray() = ByteArray(items.sumBy { it.totalLength }).apply {
        val buffer = ByteBuffer.wrap(this)
        items.forEach { it.writeData(buffer) }
    }

}