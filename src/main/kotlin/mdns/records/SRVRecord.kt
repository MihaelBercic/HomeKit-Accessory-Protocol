package mdns.records

import mdns.records.structure.CompleteRecord
import mdns.records.structure.RecordType
import java.nio.ByteBuffer

/**
 * Created by Mihael Valentin Berčič
 * on 20/12/2020 at 22:43
 * using IntelliJ IDEA
 */


class SRVRecord(override val label: String, block: SRVRecord.() -> Unit = {}) : CompleteRecord {

    init {
        apply(block)
    }

    var priority: Int = 0
    var weight: Int = 0
    var port: Int = 0
    lateinit var target: String

    override val hasProperty = false
    override val type = RecordType.SRV

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

    override fun readData(buffer: ByteBuffer) {
        val dataLength = buffer.short
        println("Skipping $dataLength")
        buffer.position(buffer.position() + dataLength)
    }
}
