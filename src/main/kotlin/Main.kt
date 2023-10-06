import homekit.HomeKitServer
import homekit.Settings
import utils.Logger
import utils.generateMAC
import utils.readOrCompute
import java.util.Timer
import kotlin.concurrent.schedule

fun main() {
    startHomeKit()
}

fun startHomeKit() {
    Logger.info("Attempting to run HomeKit.")
    try {
        val settings = readOrCompute("settings.json") {
            Settings(1, 3000, generateMAC())
        }
        HomeKitServer(settings).start()
    } catch (e: Exception) {
        e.printStackTrace()
        Logger.info("Retrying in 5 seconds...")
        Timer().schedule(5000) { startHomeKit() }
    }
}