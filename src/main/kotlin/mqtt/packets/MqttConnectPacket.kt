package mqtt.packets

import mqtt.MqttPacket
import mqtt.PacketType
import utils.Logger
import java.nio.ByteBuffer

/**
 * Created by Mihael Valentin Berčič
 * on 23/03/2021 at 13:31
 * using IntelliJ IDEA
 */
class MqttConnectPacket : MqttPacket {

    val variableHeader: VariableHeader

    constructor(fixedHeader: FixedHeader, data: ByteArray) {
        val buffer = ByteBuffer.wrap(data)
        val protocolName = buffer.string
        val protocolVersion = buffer.byte
        val connectFlags = buffer.byte
        Logger.trace(Integer.toBinaryString(connectFlags).padStart(8, '0'))

        val hasUsername = connectFlags isBitSet 7
        val hasPassword = connectFlags isBitSet 6
        val willRetain = connectFlags isBitSet 5
        val qualityOfService = (connectFlags shr 3) and 0b11
        val willFlag = connectFlags isBitSet 2
        val isCleanStart = connectFlags isBitSet 1
        if (connectFlags isBitSet 0) throw Exception("Malformed packet. Reserved flag is not 0.")

        val keepAlive = buffer.short.toInt() and 0xFFFF
        val propertiesLength = buffer.variableInteger

        // Payload
        val clientId = buffer.string

        Logger.info(
            "\n\tProtocol: $protocolName"
                    + "\n\tProtocol Version: $protocolVersion"
                    + "\n\tHas Username: $hasUsername"
                    + "\n\tHas Password: $hasPassword"
                    + "\n\tWill retain: $willRetain"
                    + "\n\tQoS: $qualityOfService"
                    + "\n\tWill flag: $willFlag"
                    + "\n\tIs clean start? $isCleanStart"
                    + "\n\t----------------------------------"
                    + "\n\tKeep alive: $keepAlive"
                    + "\n\tProperties length: $propertiesLength"
                    + "\n\tIdentifier: $clientId"

        )

        Logger.trace(buffer.remaining())
        variableHeader = VariableHeader()
    }

    constructor(fixedHeader: FixedHeader, variableHeader: VariableHeader) {
        this.variableHeader = variableHeader
    }

}

data class FixedHeader(val type: PacketType, val flags: Int)

class VariableHeader(val array: ByteArray = ByteArray(0)) {

    val data = array.toMutableList()

    fun addVariableInteger(number: Int) {
        var x = number
        do {
            var byte = number % 128
            x /= 128
            if (x > 0) byte = byte or 128
            data.add(byte.toByte())
        } while (x > 0)
    }
}