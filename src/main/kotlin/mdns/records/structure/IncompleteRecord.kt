package mdns.records.structure

import utils.minimumBytes
import java.nio.ByteBuffer
import java.util.*

/**
 * Created by Mihael Valentin Berčič
 * on 20/12/2020 at 22:45
 * using IntelliJ IDEA
 */
open class IncompleteRecord(val label: String, val type: RecordType, val hasProperty: Boolean = false) {

    infix fun String.encodeLabelInto(byteBuffer: ByteBuffer) = split(".").forEach {
        byteBuffer.put(it.length.toByte())
        byteBuffer.put(it.toByteArray())
    }

    open fun writeTo(byteBuffer: ByteBuffer) {
        val classCode = BitSet(16).apply {
            set(15, hasProperty)
            set(0, true)
        }

        byteBuffer.apply {
            label encodeLabelInto this
            put(0)
            putShort(type.id)
            put(classCode.minimumBytes(2))
        }
    }

    override fun toString(): String = "$label [$type]"
}