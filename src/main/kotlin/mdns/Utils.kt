package mdns

import java.nio.ByteBuffer
import java.util.*
import kotlin.math.pow

/**
 * Created by Mihael Valentin Berčič
 * on 19/12/2020 at 22:52
 * using IntelliJ IDEA
 */

fun BitSet.minimumBytes(n: Int): ByteArray {
    val byteArray = toByteArray()
    val difference = n - byteArray.size
    if (difference == 0) return byteArray.reversedArray()
    return byteArray.toMutableList().apply { addAll(ByteArray(difference).toTypedArray()) }.toByteArray()
        .reversedArray()
}

fun ByteBuffer.readEncodedLabel(): String {
    val characters = mutableListOf<Byte>()
    val dotAsByte = '.'.toByte()
    var returnTo = 0
    do {
        val nextByte = get()
        when {
            nextByte.isCharacter -> characters.add(nextByte)
            nextByte.isLength -> if (characters.isNotEmpty()) characters.add(dotAsByte)
            nextByte.isPointer -> {
                val jumpPosition = get().toInt() and 255
                if (returnTo == 0) returnTo = position()
                position(jumpPosition)
            }
        }
    } while (nextByte.toInt() != 0)
    if (returnTo > 0) position(returnTo)
    return String(characters.toByteArray())
}


val Byte.isPointer get() = (asString == "11000000")
val Byte.isCharacter get() = this in 30..255
val Byte.isLength get() = this in 1..63
fun Int.bits(from: Int, count: Int): Int = (this shr from) and (2.0.pow(count) - 1).toInt()
val ByteArray.asString
    get() = take(512).map { it.asString.padStart(8, '0') }.chunked(4).joinToString("\n\t") { it.joinToString(" ") }

val Byte.asString get() = Integer.toBinaryString(toInt() and 255)

