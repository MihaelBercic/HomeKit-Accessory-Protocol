package mdns.packet

import java.nio.ByteBuffer
import kotlin.math.pow

/**
 * Created by Mihael Valentin Berčič
 * on 21/12/2020 at 16:51
 * using IntelliJ IDEA
 */
class MulticastDnsHeaderReader(private val buffer: ByteBuffer) {

    fun readHeader(): MulticastDnsPacketHeader {
        val id = buffer.short
        val flags = buffer.short.toInt()
        val isResponse = flags.bits(15, 1) == 1
        val opcode = flags.bits(11, 4)
        val isAuthoritative = flags.bits(10, 1) == 1
        val isTruncated = flags.bits(9, 1) == 1
        val isRecursionDesired = flags.bits(8, 1) == 1
        val isRecursionAvailable = flags.bits(7, 1) == 1
        val responseCode = flags.bits(0, 4)

        return MulticastDnsPacketHeader(id, isResponse, opcode, isAuthoritative, isTruncated, isRecursionDesired, isRecursionAvailable, responseCode)
    }

    private fun Int.bits(from: Int, count: Int): Int = (this shr from) and (2.0.pow(count) - 1).toInt()

}