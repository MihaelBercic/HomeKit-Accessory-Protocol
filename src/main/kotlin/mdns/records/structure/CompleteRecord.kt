package mdns.records.structure

import java.nio.ByteBuffer

/**
 * Created by Mihael Valentin Berčič
 * on 20/12/2020 at 22:45
 * using IntelliJ IDEA
 *
 * I believe these classes could have been done much more effectively and clean, but I wasn't able to come up with a
 * solid solution. Yet.
 */
open class CompleteRecord(label: String, type: RecordType, hasProperty: Boolean, val timeToLive: Int) : IncompleteRecord(label, type, hasProperty) {

    protected open fun writeData(buffer: ByteBuffer) {}
    protected open fun readData(dataLength: Int, buffer: ByteBuffer) {
        buffer.position(buffer.position() + dataLength)
    }

    override fun writeTo(byteBuffer: ByteBuffer) {
        super.writeTo(byteBuffer)
        byteBuffer.putInt(timeToLive)
        writeData(byteBuffer)
    }

}