package shelly

import Logger
import gson
import homekit.communication.structure.AppleServices
import homekit.communication.structure.CharacteristicType
import homekit.communication.structure.data.ChangeRequest
import homekit.structure.Accessory
import java.net.URL
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

class ShellyBulb(aid: Int, ip: String) : Accessory(aid, ip) {

    override
    fun setup(configurationDetails: Map<String, Any>) {
        service(2, AppleServices.LightBulb).apply {
            registerInformation("Bulb", "1.0.0", "1.0.0", "Mihael", "LightBulb", "ABCDEFG") {
                Logger.info("Identifying our light bulb!")
            }

            val status = try {
                gson.fromJson(URL("http://$ip/light/0").readText(), ShellyBulbStatus::class.java)
            } catch (e: Exception) {
                ShellyBulbStatus(true, 100, 0, 50)
            }

            addCharacteristic(CharacteristicType.On, status.isOn, supportsEvents = true) {
                actions["turn"] = if (value == true) "on" else "off"
            }

            addCharacteristic(CharacteristicType.Brightness, status.brightness) {
                if (value != null) actions["brightness"] = value!!
            }
            val kelvins = 1_000_000
            val currentTemperature = kelvins / status.temperature

            addCharacteristic(CharacteristicType.ColorTemperature, currentTemperature) {
                (value as? Int)?.apply {
                    actions["temp"] = Integer.min(6500, Integer.max(3000, kelvins / this))
                }
            }

        }
    }

    val executor = Executors.newSingleThreadScheduledExecutor()
    var scheduledFuture: ScheduledFuture<out Any>? = null

    override fun commitChanges(changeRequests: List<ChangeRequest>) {
        if (actions.isNotEmpty()) {
            val toSend = actions.map { (key, value) -> "$key=$value" }.joinToString("&")
            actions.clear()

            scheduledFuture?.cancel(true)
            scheduledFuture = executor.schedule({ URL("http://$ip/light/0?$toSend").readText() }, 500, TimeUnit.MILLISECONDS)
        }
    }

}