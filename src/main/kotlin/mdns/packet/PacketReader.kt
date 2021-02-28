package mdns.packet

import Logger
import asHexString
import mdns.Packet
import mdns.records.*
import mdns.records.structure.CompleteRecord
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
    private val header = HeaderReader(buffer).readHeader()

    private val questionCount = buffer.short
    private val answerCount = buffer.short
    private val authorityCount = buffer.short
    private val additionalCount = buffer.short

    fun buildPacket(): Packet {
        try {
            val packet = Packet(header)
            for (i in 0 until questionCount) readRecordInformation().apply {
                if (classCode == 1) packet.queryRecords.add(QueryRecord(label, type, hasProperty))
            }

            for (i in 0 until answerCount) parseCompleteRecord(packet.answerRecords)
            for (i in 0 until authorityCount) parseCompleteRecord(packet.authorityRecords)
            for (i in 0 until additionalCount) parseCompleteRecord(packet.additionalRecords)
            return packet
        } catch (e: Exception) {
            e.printStackTrace()
            Logger.info("Remaining: " + buffer.remaining())
            Logger.info("Size: " + buffer.array().size)
            println(buffer.array().asHexString)
        }
        return throw Exception("")
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

    private fun parseCompleteRecord(list: MutableList<CompleteRecord>) {
        val info = readRecordInformation()
        val label = info.label
        val type = info.type
        if (info.classCode != 1) return
        val timeToLive = buffer.int
        val dataLength = buffer.short
        val record = when (type) {
            RecordType.A -> ARecord(label)
            RecordType.PTR -> PTRRecord(label)
            RecordType.TXT -> TXTRecord(label)
            RecordType.SRV -> SRVRecord(label)
            else -> null
        }
        record?.apply {
            hasProperty = info.hasProperty
            readData(buffer)
            list.add(this)
        }
        // Logger.error("Skipping over for $dataLength")
        buffer.position(buffer.position() + dataLength)
        // Logger.debug("Record read: [${record?.type}] ${record?.label}")
    }
}