package mdns

import Logger
import java.net.*


/**
 * Created by Mihael Valentin Berčič
 * on 21/12/2020 at 13:42
 * using IntelliJ IDEA
 */
open class MulticastService(val protocol: String, val localhost: InetAddress) {

    private val mDNS = 5353
    private val destination = InetAddress.getByName("224.0.0.251")
    private val inetSocketAddress = InetSocketAddress(destination, mDNS)
    private val myNetworkInterface = NetworkInterface.getByInetAddress(localhost)
    open var onDiscovery: MulticastSocket.() -> Unit = {}
    open var responseCondition: Packet.() -> Boolean = {
        queryRecords.any { it.label.contains(protocol) } && answerRecords.none { it.label.contains(protocol) }
    }

    fun startAdvertising() {
        Logger.info("Starting service advertising!")
        MulticastSocket(mDNS).apply {
            joinGroup(inetSocketAddress, myNetworkInterface)
            apply(onDiscovery)
            Thread {
                val byteArray = ByteArray(9000)
                val datagramPacket = DatagramPacket(byteArray, byteArray.size)
                while (true) {
                    receive(datagramPacket)
                    if (responseCondition(datagramPacket.asPacket)) apply(onDiscovery)
                }
                close()
            }.start()
        }
    }

}