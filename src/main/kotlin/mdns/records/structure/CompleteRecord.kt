package mdns.records.structure

import java.nio.ByteBuffer

/**
 * Created by Mihael Valentin Berčič
 * on 20/12/2020 at 22:45
 * using IntelliJ IDEA
 */
abstract class CompleteRecord : IncompleteRecord() {

    var timeToLive: Int = 4500
    open fun writeData(buffer: ByteBuffer) {}
    open fun readData(buffer: ByteBuffer) {}

    override fun writeTo(byteBuffer: ByteBuffer) {
        super.writeTo(byteBuffer)
        byteBuffer.putInt(timeToLive)
        writeData(byteBuffer)
    }

}