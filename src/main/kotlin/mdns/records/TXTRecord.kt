package mdns.records

import mdns.records.structure.CompleteRecord
import mdns.records.structure.RecordType
import java.nio.ByteBuffer

/**
 * Created by Mihael Valentin Berčič
 * on 20/12/2020 at 22:43
 * using IntelliJ IDEA
 */


class TXTRecord(override val label: String, block: MutableMap<Any, Any>.() -> Unit = {}) : CompleteRecord {

    private val cleanupRegex = "[(,\\s){}]".toRegex()
    private val dataMap: MutableMap<Any, Any> = mutableMapOf<Any, Any>().apply(block)
    override val type = RecordType.TXT
    override val hasProperty = true

    override fun writeData(buffer: ByteBuffer) {
        val dataLength = dataMap.size + dataMap.toString().replace(cleanupRegex, "").length
        buffer.putShort(dataLength.toShort())
        dataMap.forEach { (key, value) ->
            val string = "$key=$value"
            val length = string.length
            buffer.put((length and 255).toByte())
            buffer.put(string.toByteArray())
        }
    }
}
