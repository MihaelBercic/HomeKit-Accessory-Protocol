package mdns

import destination
import mDNS
import java.net.DatagramPacket
import java.net.MulticastSocket

/**
 * Created by Mihael Valentin Berčič
 * on 21/12/2020 at 13:42
 * using IntelliJ IDEA
 */
class Service(val name: String) {

    var broadcastTime = 10000
    var onDiscovery: Packet.() -> Unit = {}

    fun startAdvertising() {
        val socket = MulticastSocket(mDNS).apply {
            joinGroup(destination)
        }

        Thread {
            val start = System.currentTimeMillis()
            val byteArray = ByteArray(9000)
            val datagramPacket = DatagramPacket(byteArray, byteArray.size)
            while (System.currentTimeMillis() - broadcastTime < start) {
                socket.receive(datagramPacket)
                val packet = datagramPacket.asPacket
            }
        }.start()
    }

}