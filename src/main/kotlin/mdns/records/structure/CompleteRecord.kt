package mdns.records.structure

import java.nio.ByteBuffer

/**
 * Created by Mihael Valentin Berčič
 * on 20/12/2020 at 22:45
 * using IntelliJ IDEA
 */
interface CompleteRecord : IncompleteRecord {

    val timeToLive: Int get() = 300
    fun writeData(buffer: ByteBuffer) {}
    fun readData(buffer: ByteBuffer) {}

    override fun writeTo(byteBuffer: ByteBuffer) {
        super.writeTo(byteBuffer)
        byteBuffer.putInt(timeToLive)
        writeData(byteBuffer)
    }
}