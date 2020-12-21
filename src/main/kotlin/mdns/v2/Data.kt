package mdns.v2

import destination
import mDNS
import mdns.*
import mdns.v2.records.PTRRecord
import mdns.v2.records.QueryRecord
import mdns.v2.records.TXTRecord
import mdns.v2.records.structure.CompleteRecord
import mdns.v2.records.structure.IncompleteRecord
import mdns.v2.records.structure.RecordType
import java.net.DatagramPacket
import java.nio.ByteBuffer
import java.util.*

/**
 * Created by Mihael Valentin Berčič
 * on 19/12/2020 at 16:11
 * using IntelliJ IDEA
 */

data class Packet(
    val header: Header,
    val questionRecords: MutableList<IncompleteRecord> = mutableListOf(),
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


val Packet.asDatagramPacket
    get(): DatagramPacket {
        val flags = BitSet(16).apply {
            set(15, header.isResponse)
            set(11, header.isAuthoritativeAnswer)
            set(10, header.isTruncated)
            set(9, header.isRecursionDesired)
            set(8, header.isRecursionAvailable)
        }.minimumBytes(2)

        val byteBuffer = ByteBuffer.allocate(9000).apply {
            putShort(header.identification)
            put(flags)
            putShort(questionRecords.size.toShort())
            putShort(answerRecords.size.toShort())
            putShort(authorityRecords.size.toShort())
            putShort(additionalRecords.size.toShort())
            questionRecords.forEach { it.writeTo(this) }
            answerRecords.forEach { it.writeTo(this) }
            authorityRecords.forEach { it.writeTo(this) }
            additionalRecords.forEach { it.writeTo(this) }
        }

        val bufferArray = byteBuffer.array().dropLast(byteBuffer.remaining() - 5).toByteArray()
        return DatagramPacket(bufferArray, bufferArray.size, destination, mDNS)
    }

fun Packet.parseIncompleteRecords(count: Int, buffer: ByteBuffer) {

}

val DatagramPacket.asPacket
    get() = let {
        val buffer = ByteBuffer.wrap(it.data)
        val id = buffer.short
        val flags = buffer.short.toInt()
        val isResponse = flags.bits(15, 1) == 1
        val opcode = flags.bits(11, 4)
        val isAuthoritative = flags.bits(10, 1) == 1
        val isTruncated = flags.bits(9, 1) == 1
        val isRecursionDesired = flags.bits(8, 1) == 1
        val isRecursionAvailable = flags.bits(7, 1) == 1
        val responseCode = flags.bits(0, 4)

        val header = Header(id, isResponse, opcode, isAuthoritative, isTruncated, isRecursionDesired, isRecursionAvailable, responseCode)
        val packet = Packet(header)

        val questionCount = buffer.short.toInt()
        val answerCount = buffer.short.toInt()
        val authorityCount = buffer.short.toInt()
        val additionalCount = buffer.short.toInt()
        for (i in 0 until questionCount) {
            val label = readData(buffer)
            val type = RecordType.typeOf(buffer.short)
            val classCode = buffer.short.toInt() and 255
            if (classCode == 1) packet.questionRecords.add(QueryRecord(label, type))
        }

        for (i in 0 until answerCount + authorityCount + additionalCount) {
            val label = readData(buffer)
            val type = RecordType.typeOf(buffer.short)
            val classCode = buffer.short.toInt() and 255
            if (classCode != 1) continue
            when (type) {
                RecordType.PTR -> PTRRecord(label).readData(buffer)
                RecordType.TXT -> TXTRecord(label).readData(buffer)
            }
        }


        packet
    }

private fun readData(buffer: ByteBuffer): String {
    val characters = mutableListOf<Byte>()
    val dotAsByte = '.'.toByte()
    var returnTo = 0
    do {
        val nextByte = buffer.get()
        when {
            nextByte.isCharacter -> characters.add(nextByte)
            nextByte.isLength -> if (characters.isNotEmpty()) characters.add(dotAsByte)
            nextByte.isPointer -> {
                val jumpPosition = buffer.get().toInt() and 255
                if (returnTo == 0) returnTo = buffer.position()
                buffer.position(jumpPosition)
            }
        }
    } while (nextByte.toInt() != 0)
    if (returnTo > 0) buffer.position(returnTo)
    return String(characters.toByteArray())
}