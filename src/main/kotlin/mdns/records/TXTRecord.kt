package mdns.records

import mdns.records.structure.CompleteRecord
import mdns.records.structure.RecordType
import java.nio.ByteBuffer

/**
 * Created by Mihael Valentin Berčič
 * on 20/12/2020 at 22:43
 * using IntelliJ IDEA
 */


class TXTRecord(override val label: String) : CompleteRecord {

    private val data: MutableMap<Any, Any> = mutableMapOf()
    override val timeToLive: Int = 120
    override val hasProperty = false
    override val type = RecordType.TXT

    infix fun Any.compute(any: Any) = data.computeIfAbsent(this) { any }

    override fun writeData(buffer: ByteBuffer) {
        val dataLength = data.size + data.toString().length - 2
        buffer.putShort(dataLength.toShort())
        data.forEach { (key, value) ->
            val string = "$key=$value"
            val length = string.length
            buffer.put(length.toByte())
            buffer.put(string.toByteArray())
        }
    }

    override fun readData(buffer: ByteBuffer) {
        TODO("Not yet implemented")
    }
}
