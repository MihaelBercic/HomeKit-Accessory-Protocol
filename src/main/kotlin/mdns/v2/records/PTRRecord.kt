package mdns.v2.records

import mdns.v2.records.structure.CompleteRecord
import mdns.v2.records.structure.RecordType
import java.nio.ByteBuffer

/**
 * Created by Mihael Valentin Berčič
 * on 20/12/2020 at 22:43
 * using IntelliJ IDEA
 */


class PTRRecord(override val label: String, var domain: String = "") : CompleteRecord {

    override val timeToLive: Int = 10
    override val hasProperty = false
    override val type = RecordType.PTR

    override fun writeData(buffer: ByteBuffer) {
        val split = domain.split(".")
        val length = domain.length + split.size + 1 // + 1 for the null byte in the end.
        buffer.putShort(length.toShort())
        split.forEach { label ->
            buffer.put(label.length.toByte())
            buffer.put(label.toByteArray())
        }
        buffer.put(0)
    }

    override fun readData(buffer: ByteBuffer) {
    }
}