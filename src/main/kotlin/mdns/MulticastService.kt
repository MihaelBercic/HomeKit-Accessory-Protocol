package mdns

import java.net.DatagramPacket
import java.net.InetAddress
import java.net.MulticastSocket

/**
 * Created by Mihael Valentin Berčič
 * on 21/12/2020 at 13:42
 * using IntelliJ IDEA
 */
open class MulticastService(val protocol: String, val localhost: InetAddress) {

    private val IPv4 = "224.0.0.251"
    private val mDNS = 5353
    private val destination = InetAddress.getByName(IPv4)

    open var onDiscovery: MulticastSocket.() -> Unit = {}

    fun startAdvertising(duration: Int) {
        println("Starting service advertising!")
        MulticastSocket(mDNS).apply {
            joinGroup(destination)
            Thread {
                val start = System.currentTimeMillis()
                val byteArray = ByteArray(9000)
                val datagramPacket = DatagramPacket(byteArray, byteArray.size)
                while (System.currentTimeMillis() - duration < start) {
                    receive(datagramPacket)
                    val packet = datagramPacket.asPacket
                    if (datagramPacket.address != localhost && packet.queryRecords.any { it.label.contains(protocol) }) apply(onDiscovery)
                }
                close()
            }.start()
        }
    }

}