import homekit.HomeKitServer
import homekit.Settings
import utils.*
import java.net.HttpURLConnection
import java.net.URI
import java.net.URL
import java.net.http.HttpClient
import java.net.http.HttpRequest.BodyPublishers
import java.net.http.HttpResponse
import java.util.*
import kotlin.concurrent.schedule
import kotlin.system.exitProcess

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