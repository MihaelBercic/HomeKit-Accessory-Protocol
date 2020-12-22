package mdns.records

import mdns.records.structure.CompleteRecord
import mdns.records.structure.RecordType
import java.nio.ByteBuffer

/**
 * Created by Mihael Valentin Berčič
 * on 20/12/2020 at 22:43
 * using IntelliJ IDEA
 */


class TXTRecord(override val label: String, block: TXTRecord.() -> Unit) : CompleteRecord {

    init {
        apply(block)
    }

    private val cleanupRegex = "[(,\\s){}]".toRegex()

    private val dataMap: MutableMap<Any, Any> = mutableMapOf()
    override val hasProperty = false
    override val type = RecordType.TXT

    infix operator fun Any.rangeTo(data: Any) = dataMap.computeIfAbsent(this) { data }


    override fun writeData(buffer: ByteBuffer) {
        val dataLength = dataMap.size + dataMap.toString().replace(cleanupRegex, "").apply { println(this) }.length
        buffer.putShort(dataLength.toShort())
        println(dataLength)
        dataMap.forEach { (key, value) ->
            val string = "$key=$value"
            val length = string.length
            buffer.put(length.toByte())
            buffer.put(string.toByteArray())
        }
    }

    override fun readData(buffer: ByteBuffer) {
        val dataLength = buffer.short
        println("Skipping $dataLength")
        buffer.position(buffer.position() + dataLength)
    }
}
