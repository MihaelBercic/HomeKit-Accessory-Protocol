package mqtt

import utils.Logger
import java.nio.ByteBuffer

/**
 * Created by Mihael Valentin Berčič
 * on 21/03/2021 at 12:25
 * using IntelliJ IDEA
 */
open class MqttPacket {

    internal val ByteBuffer.variableInteger
        get(): Int {
            var multiplier = 1
            var value = 0
            do {
                val encodedByte = get().toInt() and 0xFF
                value += (encodedByte and 127) * multiplier
                if (multiplier > 128 * 128 * 128) throw Exception("Malformed")
                multiplier *= 128
            } while (encodedByte shr 7 == 1)
            return value
        }

    internal val ByteBuffer.string
        get():String {
            val length = short.toInt() and 0xFFFF
            Logger.trace("String length: $length")
            val characters = ByteArray(length)
            this[characters]
            return String(characters)
        }

    internal val ByteBuffer.byte get():Int = get().toInt() and 0xFF

    internal infix fun Int.isBitSet(position: Int) = (shr(position) and 1) == 1

}