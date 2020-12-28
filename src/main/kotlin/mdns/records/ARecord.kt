package mdns.records

import mdns.records.structure.CompleteRecord
import mdns.records.structure.RecordType
import java.nio.ByteBuffer

/**
 * Created by Mihael Valentin Berčič
 * on 22/12/2020 at 13:14
 * using IntelliJ IDEA
 */
class ARecord(override val label: String, block: ARecord.() -> Unit) : CompleteRecord {

    lateinit var address: String

    override val type: RecordType = RecordType.A
    override val hasProperty: Boolean = true

    override fun writeData(buffer: ByteBuffer) {
        val split = address.split(".")
        buffer.putShort(split.size.toShort())
        split.forEach { buffer.put(it.toInt().toByte()) }
    }

    override fun readData(buffer: ByteBuffer) {
    }

    init {
        apply(block)
    }
}