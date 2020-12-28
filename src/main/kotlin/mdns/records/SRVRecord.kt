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

    var port: Int = 0
    private var priority: Int = 0
    private var weight: Int = 0
    lateinit var target: String
    override val type = RecordType.SRV

    init {
        apply(block)
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
