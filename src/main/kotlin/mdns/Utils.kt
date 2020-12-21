package mdns

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


val Byte.isPointer get() = (asString == "11000000")
val Byte.isCharacter get() = this in 30..255
val Byte.isLength get() = this in 1..63
fun Int.bits(from: Int, count: Int): Int = (this shr from) and (2.0.pow(count) - 1).toInt()
val ByteArray.asString
    get() = take(512).map { it.asString.padStart(8, '0') }.chunked(4).joinToString("\n\t") { it.joinToString(" ") }

val Byte.asString get() = Integer.toBinaryString(toInt() and 255)
