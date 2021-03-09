package mdns.records

import mdns.records.structure.CompleteRecord
import mdns.records.structure.RecordType
import java.nio.ByteBuffer

/**
 * Created by Mihael Valentin Berčič
 * on 22/12/2020 at 13:14
 * using IntelliJ IDEA
 *
 */
class ARecord(label: String, address: String, hasProperty: Boolean, timeToLive: Int) : CompleteRecord(label, RecordType.A, hasProperty, timeToLive) {

    var address: String = address
        private set

    constructor(label: String, timeToLive: Int, dataLength: Int, buffer: ByteBuffer, hasProperty: Boolean) : this(label, "ToBeRead", hasProperty, timeToLive) {
        readData(dataLength, buffer)
    }

    override fun writeData(buffer: ByteBuffer) {
        val split = address.split(".")
        buffer.putShort(split.size.toShort())
        split.forEach { buffer.put(it.toInt().toByte()) }
    }

    override fun readData(dataLength: Int, buffer: ByteBuffer) {
        val characters = ByteArray(dataLength)
        buffer[characters]
        address = String(characters)
    }


}