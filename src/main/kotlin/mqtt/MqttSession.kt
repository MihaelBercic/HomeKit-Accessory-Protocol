package mqtt

import mqtt.packets.FixedHeader
import mqtt.packets.MqttConnectPacket
import utils.Logger
import java.net.Socket

/**
 * Created by Mihael Valentin Berčič
 * on 20/03/2021 at 15:56
 * using IntelliJ IDEA
 */
class MqttSession(private val socket: Socket) {

    var isAlive = true

    private val outputStream = socket.getOutputStream()
    private val inputStream = socket.getInputStream()

    init {
        Logger.info("New MQTT session: ${socket.remoteSocketAddress}")
        while (isAlive) {
            val typeAndFlags = inputStream.read()
            if (typeAndFlags == -1) {
                inputStream.close()
                outputStream.close()
                break
            }
            val type = PacketType.values()[typeAndFlags shr 4]
            val flags = typeAndFlags and 15
            val length = inputStream.readVariableInteger()
            val fixedHeader = FixedHeader(type, flags)
            Logger.info("$type with $flags and length $length")
            val data = inputStream.readNBytes(length)
            when (type) {
                PacketType.Connect -> MqttConnectPacket(fixedHeader, data)
                PacketType.ConnectAcknowledgment -> TODO()
                PacketType.Publish -> TODO()
                PacketType.PublishAcknowledgment -> TODO()
                PacketType.PublishReceived -> TODO()
                PacketType.PublishRelease -> TODO()
                PacketType.PublishComplete -> TODO()
                PacketType.Subscribe -> TODO()
                PacketType.SubscribeAcknowledgment -> TODO()
                PacketType.Unsubscribe -> TODO()
                PacketType.UnsubscribeAcknowledgment -> TODO()
                PacketType.PingRequest -> TODO()
                PacketType.PingResponse -> TODO()
                PacketType.Disconnect -> TODO()
                PacketType.Authentication -> TODO()
                else -> Logger.error("Message is malformed.")
            }
        }
    }
}