package homekit

import utils.gson
import java.io.File

/**
 * Created by Mihael Valentin Berčič
 * on 14/02/2021 at 13:45
 * using IntelliJ IDEA
 */
data class Settings(
    var configurationNumber: Int,
    val port: Int,
    val serverMAC: String
) {
    fun save() = File("settings.json").writeText(gson.toJson(this))
    fun increaseConfiguration(){
        configurationNumber++
        save()
    }
}