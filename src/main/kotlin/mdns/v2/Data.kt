package mdns.v2

import mdns.v2.packet.DatagramPacketBuilder
import mdns.v2.packet.PacketReader
import mdns.v2.records.structure.CompleteRecord
import mdns.v2.records.structure.IncompleteRecord
import java.net.DatagramPacket

/**
 * Created by Mihael Valentin Berčič
 * on 19/12/2020 at 16:11
 * using IntelliJ IDEA
 */

data class Packet(
    val header: Header,
    val queryRecords: MutableList<IncompleteRecord> = mutableListOf(),
    val answerRecords: MutableList<CompleteRecord> = mutableListOf(),
    val authorityRecords: MutableList<CompleteRecord> = mutableListOf(),
    val additionalRecords: MutableList<CompleteRecord> = mutableListOf()
)

data class Header(
    val identification: Short,
    val isResponse: Boolean,
    val opcode: Int = 0,
    val isAuthoritativeAnswer: Boolean = false,
    val isTruncated: Boolean = false,
    val isRecursionDesired: Boolean = false,
    val isRecursionAvailable: Boolean = false,
    val responseCode: Int = 0
)

val Packet.asDatagramPacket get() = DatagramPacketBuilder(this).buildDatagramPacket()

val DatagramPacket.asPacket get() = PacketReader(this).buildPacket()