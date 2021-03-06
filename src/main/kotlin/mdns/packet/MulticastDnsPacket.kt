package mdns.packet

import mdns.records.structure.CompleteRecord
import mdns.records.structure.IncompleteRecord

/**
 * Created by Mihael Valentin Berčič
 * on 19/12/2020 at 16:11
 * using IntelliJ IDEA
 */

data class MulticastDnsPacket(
    val header: MulticastDnsPacketHeader,
    val queryRecords: MutableList<IncompleteRecord> = mutableListOf(),
    val answerRecords: MutableList<CompleteRecord> = mutableListOf(),
    val authorityRecords: MutableList<CompleteRecord> = mutableListOf(),
    val additionalRecords: MutableList<CompleteRecord> = mutableListOf()
) {
    val asDatagramPacket get() = DatagramPacketBuilder(this).buildDatagramPacket()
}