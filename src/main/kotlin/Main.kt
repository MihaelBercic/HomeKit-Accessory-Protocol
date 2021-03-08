import homekit.HomeKitServer
import homekit.HomeKitService
import homekit.Settings
import utils.generateMAC
import utils.readOrCompute
import java.net.InetAddress


fun main() {
    val settings = readOrCompute("settings.json") { Settings(1, 3000, generateMAC()) }
    HomeKitServer(settings).start()
}