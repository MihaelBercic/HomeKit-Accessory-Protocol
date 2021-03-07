package shelly

import homekit.communication.structure.AppleServices
import homekit.communication.structure.CharacteristicType
import homekit.communication.structure.data.ChangeRequest
import homekit.structure.Accessory
import utils.Logger
import utils.gson
import java.net.URL
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

class ShellyBulb(aid: Int, ip: String) : Accessory(aid, ip) {

    private val actions = mutableMapOf<String, Any>()
    private val executor = Executors.newSingleThreadScheduledExecutor()
    private var scheduledFuture: ScheduledFuture<out Any>? = null
    private val statusPath = "http://$ip/light/0"

    override
    fun setup(configurationDetails: Map<String, Any>) {
        service(2, AppleServices.LightBulb).apply {
            registerInformation("Bulb", "1.0.0", "1.0.0", "Mihael", "LightBulb", "ABCDEFG") {
                Logger.info("Identifying our light bulb!")
            }

            val status = try {
                gson.fromJson(URL(statusPath).readText(), ShellyBulbStatus::class.java)
            } catch (e: Exception) {
                ShellyBulbStatus(50, 100, true, 50)
            }

            addCharacteristic(CharacteristicType.On, status.isOn, supportsEvents = true) {
                actions["turn"] = if (value == true) "on" else "off"
            }

            addCharacteristic(CharacteristicType.Brightness, status.brightness) {
                if (value != null && (value as Int) > 0) actions["brightness"] = value!!
            }

        }
    }

    override fun commitChanges(changeRequests: List<ChangeRequest>) {
        if (actions.isNotEmpty()) {
            val toSend = actions.map { (key, value) -> "$key=$value" }.joinToString("&")
            actions.clear()
            Logger.info("Sending $toSend to our light!")

            scheduledFuture?.cancel(true)
            scheduledFuture = executor.schedule({ URL("http://$ip/light/0?$toSend").readText() }, 250, TimeUnit.MILLISECONDS)
        }
    }

}