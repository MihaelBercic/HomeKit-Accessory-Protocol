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
class PTRRecord(label: String, domain: String, isCached: Boolean, timeToLive: Int) : CompleteRecord(label, RecordType.PTR, isCached, timeToLive) {

    var domain: String = domain
        private set

    constructor(label: String, timeToLive: Int, dataLength: Int, buffer: ByteBuffer, isCached: Boolean) : this(label, "ToBeRead", isCached, timeToLive) {
        readData(dataLength, buffer)
    }

    override fun writeData(buffer: ByteBuffer) {
        val split = domain.split(".")
        val length = domain.length + 2
        buffer.putShort(length.toShort())
        split.forEach { label ->
            buffer.put(label.length.toByte())
            buffer.put(label.toByteArray())
        }
        buffer.put(0)
    }
}