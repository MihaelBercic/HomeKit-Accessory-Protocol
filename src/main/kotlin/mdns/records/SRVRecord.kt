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
class SRVRecord(
    label: String,
    target: String,
    port: Int = 80,
    priority: Int = 0,
    weight: Int = 0,
    timeToLive: Int,
    isCached: Boolean
) : CompleteRecord(label, RecordType.SRV, isCached, timeToLive) {

    var port: Int = port
        private set

    var priority: Int = priority
        private set

    var weight: Int = weight
        private set

    var target: String = target
        private set

    constructor(label: String, timeToLive: Int, dataLength: Int, buffer: ByteBuffer, isCached: Boolean) :
            this(label, "ToBeRead", timeToLive = timeToLive, isCached = isCached) {
        readData(dataLength, buffer)
    }

    override fun writeData(buffer: ByteBuffer) {
        buffer.apply {
            val length = target.length + 8
            putShort(length.toShort())
            putShort(priority.toShort())
            putShort(weight.toShort())
            putShort(port.toShort())
            target encodeLabelInto this
            put(0)
        }
    }

}
