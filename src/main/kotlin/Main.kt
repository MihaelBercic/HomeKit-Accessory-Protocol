import homekit.HomeKitServer
import homekit.HomeKitService
import homekit.Settings
import utils.generateMAC
import utils.readOrCompute
import java.net.InetAddress


fun main() {
    val settings = readOrCompute("settings.json") { Settings(1, 3000, generateMAC()) }

    InetAddress.getLocalHost().apply {
        if (isLoopbackAddress) {
            throw Exception("$this is a loopback address! We can not advertise a loopback address.")
        }
    }

    HomeKitServer(settings).start()
    HomeKitService(settings).startAdvertising()
}