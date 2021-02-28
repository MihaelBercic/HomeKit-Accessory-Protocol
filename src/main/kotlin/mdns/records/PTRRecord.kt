package mdns.records

import mdns.records.structure.CompleteRecord
import mdns.records.structure.RecordType
import java.nio.ByteBuffer

/**
 * Created by Mihael Valentin Berčič
 * on 20/12/2020 at 22:43
 * using IntelliJ IDEA
 */


class PTRRecord(override var label: String, block: PTRRecord.() -> Unit = {}) : CompleteRecord() {

    init {
        apply(block)
    }

    lateinit var domain: String

    override val type = RecordType.PTR

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