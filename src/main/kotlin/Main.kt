import homekit.HomeKitServer
import homekit.Settings
import utils.Logger
import utils.generateMAC
import utils.readOrCompute
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress


private fun runSocket(port: Int, initialPacket: DatagramPacket? = null, block: (socket: DatagramSocket, packet: DatagramPacket) -> Unit) {
    Thread {
        val data = ByteArray(10)
        val socket = DatagramSocket(port)
        val packet = DatagramPacket(data, data.size)
        initialPacket?.apply { socket.send(this) }
        while (true) {
            packet.data = data
            socket.receive(packet)
            block(socket, packet)
        }
    }.start()
}


fun main() {
    runSocket(1337) { socket, packet ->
        Logger.trace(String(packet.data))
        packet.port = 1338
        packet.data = "kikiriki".toByteArray()
        socket.send(packet)
    }

    val initialMessage = "hey".toByteArray()
    val helloPacket = DatagramPacket(initialMessage, initialMessage.size, InetAddress.getLocalHost(), 1337)

    val stronk = (0..1500).map { 'a' }.joinToString("")
    println(stronk)
    runSocket(1338, helloPacket) { socket, packet ->
        Logger.info(String(packet.data))
        Thread.sleep(1000)
        packet.port = 1337
        packet.data = stronk.toByteArray()
        socket.send(packet)
    }
}

fun test() {
    val settings = readOrCompute("settings.json") {
        Settings(1, 3000, generateMAC())
    }
    HomeKitServer(settings).start()
}