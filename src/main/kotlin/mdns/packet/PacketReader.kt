package mdns.packet

import mdns.records.ARecord
import mdns.records.PTRRecord
import mdns.records.SRVRecord
import mdns.records.TXTRecord
import mdns.records.structure.CompleteRecord
import mdns.records.structure.IncompleteRecord
import mdns.records.structure.RecordType
import java.net.DatagramPacket
import java.nio.ByteBuffer

/**
 * Created by Mihael Valentin Berčič
 * on 21/12/2020 at 16:23
 * using IntelliJ IDEA
 */
class PacketReader(datagramPacket: DatagramPacket) {

    private data class RecordInformation(val label: String, val type: RecordType, val classCode: Int, val hasProperty: Boolean)

    private val buffer = ByteBuffer.wrap(datagramPacket.data)
    private val header = MulticastDnsHeaderReader(buffer).readHeader()

    private val questionCount = buffer.short
    private val answerCount = buffer.short
    private val authorityCount = buffer.short
    private val additionalCount = buffer.short

    fun buildPacket(): MulticastDnsPacket = MulticastDnsPacket(header).apply {
        for (i in 0 until questionCount) queryRecords.add(parseIncompleteRecord())
        for (i in 0 until answerCount) answerRecords.add(parseCompleteRecord())
        for (i in 0 until authorityCount) authorityRecords.add(parseCompleteRecord())
        for (i in 0 until additionalCount) additionalRecords.add(parseCompleteRecord())
    }

    private fun readRecordInformation(): RecordInformation {
        val label = buffer.readEncodedLabel()
        val type = RecordType.typeOf(buffer.short)
        val flags = buffer.short.toInt() and 0xFFFF
        val classCode = flags and 0b0111_1111_1111_1111
        val hasProperty = (flags shr 15) == 1
        return RecordInformation(label, type, classCode, hasProperty)
    }

    private fun ByteBuffer.readEncodedLabel(): String {
        val characters = mutableListOf<Byte>()
        val dotAsByte = '.'.toByte()
        var returnTo = 0
        do {
            val byte = get()
            val asInt = byte.toInt() and 0xFF
            val isPointer = asInt >= 0b11000000
            if (asInt == 0) break
            if (isPointer) {
                val shifted = (asInt and 0b00111111) shl 8
                val position = shifted or (get().toInt() and 0xFF)
                if (returnTo == 0) returnTo = position()
                position(position)
            } else {
                val label = ByteArray(asInt)
                // Logger.info("Reading a label of length $asInt with ${remaining()} remaining")
                this[label]
                if (characters.isNotEmpty()) characters.add(dotAsByte)
                characters.addAll(label.toTypedArray())
                // Logger.info("Label read: ${String(label)}")
            }
        } while (hasRemaining())
        if (returnTo > 0) position(returnTo)
        return String(characters.toByteArray())
    }

    private fun parseIncompleteRecord(): IncompleteRecord {
        val info = readRecordInformation()
        val label = info.label
        val type = info.type
        return IncompleteRecord(label, type, info.hasProperty)
    }

    private fun parseCompleteRecord(): CompleteRecord {
        val info = readRecordInformation()
        val label = info.label
        val type = info.type
        val isCached = info.hasProperty
        val timeToLive = buffer.int
        val dataLength = buffer.short.toInt()
        return when (type) {
            RecordType.A -> ARecord(label, timeToLive, dataLength, buffer, isCached)
            RecordType.PTR -> PTRRecord(label, timeToLive, dataLength, buffer, isCached)
            RecordType.TXT -> TXTRecord(label, timeToLive, dataLength, buffer, isCached)
            RecordType.SRV -> SRVRecord(label, timeToLive, dataLength, buffer, isCached)
            else -> CompleteRecord(label, RecordType.Unsupported, isCached, timeToLive)
        }
    }
}