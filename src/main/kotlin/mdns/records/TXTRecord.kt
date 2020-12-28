package mdns.records

import mdns.records.structure.CompleteRecord
import mdns.records.structure.RecordType
import java.nio.ByteBuffer

/**
 * Created by Mihael Valentin Berčič
 * on 20/12/2020 at 22:43
 * using IntelliJ IDEA
 */


class TXTRecord(override val label: String, block: TXTRecord.() -> Unit = {}) : CompleteRecord {

    private val cleanupRegex = "[(,\\s){}]".toRegex()
    private val dataMap: MutableMap<Any, Any> = mutableMapOf()
    override val type = RecordType.TXT
    override val hasProperty = true

    infix operator fun Any.rangeTo(data: Any) = dataMap.computeIfAbsent(this) { data }

    init {
        apply(block)
    }

    override fun writeData(buffer: ByteBuffer) {
        val dataLength = dataMap.size + dataMap.toString().replace(cleanupRegex, "").length
        buffer.putShort(dataLength.toShort())
        dataMap.forEach { (key, value) ->
            val string = "$key=$value"
            val length = string.length
            buffer.put(length.toByte())
            buffer.put(string.toByteArray())
        }
    }
}
