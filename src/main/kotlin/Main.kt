import com.google.gson.Gson
import com.google.gson.GsonBuilder
import homekit.HomeKitServer
import homekit.HomeKitService
import homekit.Settings


/**
 * Created by Mihael Valentin Berčič
 * on 08/12/2020 at 22:41
 * using IntelliJ IDEA
 */

val gson: Gson = GsonBuilder().create()
val appleGson: Gson = GsonBuilder().excludeFieldsWithoutExposeAnnotation().create()


fun main() {
    val settings = readOrCompute("settings.json") { Settings(1, 3000, generateMAC()) }
    HomeKitServer(settings).start()
    HomeKitService(settings).startAdvertising()
}