package mdns

import Logger
import java.net.*


/**
 * Created by Mihael Valentin Berčič
 * on 21/12/2020 at 13:42
 * using IntelliJ IDEA
 */
abstract class MulticastService(val protocol: String, val localhost: InetAddress) {

    private val mDNS = 5353
    private val destination = InetAddress.getByName("224.0.0.251")
    private val inetSocketAddress = InetSocketAddress(destination, mDNS)
    private val myNetworkInterface = NetworkInterface.getByInetAddress(localhost)

    open var responseCondition: Packet.() -> Boolean = { false }
    abstract var respondWith: (socket: MulticastSocket, receivedPacket: DatagramPacket, asPacket: Packet) -> Unit

    open val wakeUpPacket: Packet? = null

    fun startAdvertising() {
        Logger.info("Starting service advertising!")
        MulticastSocket(mDNS).apply {
            timeToLive = 255
            joinGroup(inetSocketAddress, myNetworkInterface)

            Thread {
                val byteArray = ByteArray(9000)
                val datagramPacket = DatagramPacket(byteArray, byteArray.size)

                val packet = wakeUpPacket?.asDatagramPacket
                if (packet != null) send(packet)
                while (true) {
                    datagramPacket.data = byteArray
                    receive(datagramPacket)
                    val receivedPacket = datagramPacket.asPacket
                    if (responseCondition(receivedPacket)) respondWith(this, datagramPacket, receivedPacket)
                }
                close()
            }.start()
        }
    }

}