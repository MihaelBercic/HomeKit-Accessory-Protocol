package mdns.records

import mdns.records.structure.CompleteRecord
import mdns.records.structure.RecordType
import java.nio.ByteBuffer

/**
 * Created by Mihael Valentin Berčič
 * on 20/12/2020 at 22:43
 * using IntelliJ IDEA
 *
 * I believe these classes could have been done much more effectively and clean, but I wasn't able to come up with a
 * solid solution. Yet.
 */
class TXTRecord(label: String, isCached: Boolean, timeToLive: Int, block: MutableMap<Any, Any>.() -> Unit = {}) : CompleteRecord(label, RecordType.TXT, isCached, timeToLive) {

    val dataMap: MutableMap<Any, Any> = mutableMapOf<Any, Any>().apply(block)
    private val cleanupRegex = "[(,\\s){}]".toRegex()

    constructor(label: String, timeToLive: Int, dataLength: Int, buffer: ByteBuffer, isCached: Boolean) : this(label, isCached, timeToLive) {
        readData(dataLength, buffer)
    }

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
