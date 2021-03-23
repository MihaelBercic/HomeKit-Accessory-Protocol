package mqtt

import java.io.InputStream

/**
 * Created by Mihael Valentin Berčič
 * on 21/03/2021 at 22:37
 * using IntelliJ IDEA
 */

fun InputStream.readVariableInteger(): Int {
    var multiplier = 1
    var value = 0
    do {
        val encodedByte = read()
        value += (encodedByte and 127) * multiplier
        if (multiplier > 128 * 128 * 128) throw Exception("Malformed")
        multiplier *= 128
    } while (encodedByte shr 7 == 1)
    return value
}