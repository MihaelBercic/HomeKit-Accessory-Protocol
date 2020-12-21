package mdns.packet

import mdns.bits
import mdns.Header
import java.nio.ByteBuffer

/**
 * Created by Mihael Valentin Berčič
 * on 21/12/2020 at 16:51
 * using IntelliJ IDEA
 */
class HeaderReader(private val buffer: ByteBuffer) {

    fun readHeader(): Header {
        val id = buffer.short
        val flags = buffer.short.toInt()
        val isResponse = flags.bits(15, 1) == 1
        val opcode = flags.bits(11, 4)
        val isAuthoritative = flags.bits(10, 1) == 1
        val isTruncated = flags.bits(9, 1) == 1
        val isRecursionDesired = flags.bits(8, 1) == 1
        val isRecursionAvailable = flags.bits(7, 1) == 1
        val responseCode = flags.bits(0, 4)

        return Header(id, isResponse, opcode, isAuthoritative, isTruncated, isRecursionDesired, isRecursionAvailable, responseCode)
    }

}