package mdns.packet

import mdns.Packet
import mdns.records.PTRRecord
import mdns.records.QueryRecord
import mdns.records.SRVRecord
import mdns.records.TXTRecord
import mdns.records.structure.CompleteRecord
import mdns.records.structure.RecordType
import readEncodedLabel
import java.net.DatagramPacket
import java.nio.ByteBuffer

/**
 * Created by Mihael Valentin Berčič
 * on 21/12/2020 at 16:23
 * using IntelliJ IDEA
 */
class PacketReader(datagramPacket: DatagramPacket) {

    private data class RecordInformation(val label: String, val type: RecordType, val classCode: Int)

    private val buffer = ByteBuffer.wrap(datagramPacket.data)
    private val header = HeaderReader(buffer).readHeader()

    private val questionCount = buffer.short
    private val answerCount = buffer.short
    private val authorityCount = buffer.short
    private val additionalCount = buffer.short

    fun buildPacket(): Packet {
        val packet = Packet(header)
        for (i in 0 until questionCount) readRecordInformation().apply {
            if (classCode == 1) packet.queryRecords.add(QueryRecord(label, type))
        }

        for (i in 0 until answerCount) parseCompleteRecord(packet.answerRecords)
        for (i in 0 until authorityCount) parseCompleteRecord(packet.authorityRecords)
        for (i in 0 until additionalCount) parseCompleteRecord(packet.additionalRecords)
        return packet
    }


    private fun readRecordInformation(): RecordInformation {
        val label = buffer.readEncodedLabel()
        val type = RecordType.typeOf(buffer.short)
        val classCode = buffer.short.toInt() and 255
        return RecordInformation(label, type, classCode)
    }

    private fun parseCompleteRecord(list: MutableList<CompleteRecord>) {
        val info = readRecordInformation()
        val label = info.label
        val type = info.type
        if (info.classCode != 1) return
        val record = when (type) {
            RecordType.PTR -> PTRRecord(label)
            RecordType.TXT -> TXTRecord(label)
            RecordType.SRV -> SRVRecord(label)
            else -> return
        }
        record.readData(buffer)
        list.add(record)
    }
}