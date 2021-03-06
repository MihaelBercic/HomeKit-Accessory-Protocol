package mdns.packet

import utils.minimumBytes
import java.net.DatagramPacket
import java.net.InetAddress
import java.nio.ByteBuffer
import java.util.*

/**
 * Created by Mihael Valentin Berčič
 * on 21/12/2020 at 16:59
 * using IntelliJ IDEA
 */
class DatagramPacketBuilder(private val packet: MulticastDnsPacket) {

    private val header = packet.header

    private fun buildFlags() = BitSet(16).apply {
        set(15, header.isResponse)
        set(11, header.isAuthoritativeAnswer)
        set(10, header.isTruncated)
        set(9, header.isRecursionDesired)
        set(8, header.isRecursionAvailable)
    }.minimumBytes(2)

    fun buildDatagramPacket(): DatagramPacket {
        val flags = buildFlags()
        val queryRecords = packet.queryRecords
        val answerRecords = packet.answerRecords
        val authorityRecords = packet.authorityRecords
        val additionalRecords = packet.additionalRecords

        val byteBuffer = ByteBuffer.allocate(9000).apply {
            putShort(header.identification)
            put(flags)
            putShort(queryRecords.size.toShort())
            putShort(answerRecords.size.toShort())
            putShort(authorityRecords.size.toShort())
            putShort(additionalRecords.size.toShort())
            queryRecords.forEach { it.writeTo(this) }
            answerRecords.forEach { it.writeTo(this) }
            authorityRecords.forEach { it.writeTo(this) }
            additionalRecords.forEach { it.writeTo(this) }
        }

        val newArray = ByteArray(byteBuffer.position() + 1)
        byteBuffer.position(0)
        byteBuffer.get(newArray)
        return DatagramPacket(newArray, newArray.size, InetAddress.getByName("224.0.0.251"), 5353)
    }

}