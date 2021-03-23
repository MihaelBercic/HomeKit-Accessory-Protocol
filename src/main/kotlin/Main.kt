import homekit.HomeKitServer
import homekit.Settings
import mqtt.Broker
import utils.generateMAC
import utils.readOrCompute

fun main() {

    Broker().startListening(6969)
    // server()
}

private fun server() {
    val settings = readOrCompute("settings.json") {
        Settings(1, 3000, generateMAC())
    }
    HomeKitServer(settings).start()
}