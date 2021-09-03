import homekit.HomeKitServer
import homekit.Settings
import utils.generateMAC
import utils.readOrCompute

fun main() {
    try {
        val settings = readOrCompute("settings.json") {
            Settings(1, 3000, generateMAC())
        }
        HomeKitServer(settings).start()
    } catch (e: Exception) {
        e.printStackTrace()
    }
}