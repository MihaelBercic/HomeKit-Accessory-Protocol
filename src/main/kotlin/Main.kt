import com.google.gson.GsonBuilder
import homekit.HomeKitServer
import homekit.HomeKitService
import homekit.Settings


/**
 * Created by Mihael Valentin Berčič
 * on 08/12/2020 at 22:41
 * using IntelliJ IDEA
 */

val gson = GsonBuilder()
    // .setPrettyPrinting()
    //.serializeNulls()
    .create()


fun main() {
    try {
        val settings = readOrCompute("settings.json") { Settings(0, 3000, generateMAC()) }
        settings.apply {
            HomeKitServer(this).start(port)
            HomeKitService(this, port).startAdvertising()
        }
    } catch (e: Exception) {
        e.printStackTrace()
        Logger.error("An error occured...")
    }
}