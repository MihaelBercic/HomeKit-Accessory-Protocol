package mdns.records

import mdns.records.structure.CompleteRecord
import mdns.records.structure.RecordType
import java.nio.ByteBuffer

/**
 * Created by Mihael Valentin Berčič
 * on 20/12/2020 at 22:43
 * using IntelliJ IDEA
 */


class SRVRecord(override val label: String) : CompleteRecord {

    var priority: Int = 0
    var weight: Int = 0
    var port: Int = 0
    lateinit var host: String

    override val timeToLive: Int = 120
    override val hasProperty = false
    override val type = RecordType.SRV

    override fun writeData(buffer: ByteBuffer) {
        buffer.apply {
            val length = host.length + 7
            putShort(length.toShort())
            putShort(priority.toShort())
            putShort(weight.toShort())
            putShort(port.toShort())
            host encodeLabelInto this
        }
    }

    override fun readData(buffer: ByteBuffer) {
        TODO("Not yet implemented")
    }
}
