package mdns

import mdns.packet.MulticastDnsPacket
import utils.Logger
import utils.asPacket
import java.net.*
import kotlin.random.Random


/**
 * Created by Mihael Valentin Berčič
 * on 21/12/2020 at 13:42
 * using IntelliJ IDEA
 *
 * This class serves as a structure to be used for specific service advertisements.
 *
 * @see <a href="https://tools.ietf.org/html/rfc6762">Multicast DNS RFC</a>
 * @see <a href="https://tools.ietf.org/html/rfc6763">DNS-Based Service Discovery RFC</a>
 *
 */
abstract class MulticastService(val protocol: String, val localhost: InetAddress) {

    private val mDNS = 5353
    private val destination = InetAddress.getByName("224.0.0.251")
    private val inetSocketAddress = InetSocketAddress(destination, mDNS)
    private val myNetworkInterface = NetworkInterface.getByInetAddress(localhost)

    abstract fun condition(packet: MulticastDnsPacket): Boolean
    abstract fun respond(socket: MulticastSocket, datagramPacket: DatagramPacket, packet: MulticastDnsPacket)

    open val wakeUpPacket: MulticastDnsPacket? = null

    /**
     * Start advertising and responding to queries looking for the specific service.
     *
     * Currently works on IPv4 with the destination of: 224.0.0.251 and a multicast port 5353
     */
    fun startAdvertising() {
        MulticastSocket(mDNS).apply {
            timeToLive = 255
            joinGroup(inetSocketAddress, myNetworkInterface)

            Thread {
                val byteArray = ByteArray(9000)
                val datagramPacket = DatagramPacket(byteArray, byteArray.size)
                val packet = wakeUpPacket?.asDatagramPacket
                if (packet != null) send(packet)
                while (true) {
                    try {
                        datagramPacket.data = byteArray
                        receive(datagramPacket)
                        val receivedPacket = datagramPacket.asPacket
                        if (condition(receivedPacket)) {
                            Thread.sleep(Random.nextLong(100, 300))
                            respond(this, datagramPacket, receivedPacket)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }.start()
        }
        Logger.info("Advertising $protocol service has started on $localhost!")
    }

}