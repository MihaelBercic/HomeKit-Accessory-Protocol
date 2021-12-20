package plugins.shelly.dimmer

import homekit.structure.Accessory
import homekit.structure.data.CharacteristicType
import homekit.structure.data.ServiceType
import utils.HttpMethod
import utils.Logger
import utils.gson
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

class ShellyBulb(aid: Long, name: String, ip: String) : Accessory(aid, name, ip) {

    private val actions = mutableMapOf<String, Any>()
    private val executor = Executors.newSingleThreadScheduledExecutor()
    private var scheduledFuture: ScheduledFuture<out Any>? = null

    override fun setup(configurationDetails: Map<String, Any>, bridgeAddress: String) {
        sendRequest(HttpMethod.GET, "/settings?transition=10&fade_rate=5&pulse_mode=2")
        sendRequest(HttpMethod.GET, "/ota?update=true")

        addService(2, ServiceType.LightBulb).apply {
            registerInformation("1.0.0", "1.0.0", "Shelly", "LightBulb", "ABCDEFG") {
                Logger.info("Identifying our light bulb!")
            }

            val onCharacteristic = add(CharacteristicType.On) {
                actions["turn"] = if (value == true) "on" else "off"
            }
            val brightnessCharacteristic = add(CharacteristicType.Brightness) {
                if (value != null && (value as Int) > 0) actions["brightness"] = value!!
            }

            val query = "${onCharacteristic.iid},${brightnessCharacteristic.iid}"
            val notificationOffUrl = "/settings/actions?index=0&name=out_off_url&enabled=true&urls[]=$bridgeAddress/event?$aid:$query"
            val notificationOnUrl = "/settings/actions?index=0&name=out_on_url&enabled=true&urls[]=$bridgeAddress/event?$aid:$query"
            sendRequest(HttpMethod.GET, notificationOffUrl)
            sendRequest(HttpMethod.GET, notificationOnUrl)
        }
    }

    override fun update() {
        sendRequest(HttpMethod.GET, "/light/0") { _, body ->
            val status = gson.fromJson(body, ShellyBulbStatus::class.java)
            getService(2) {
                set(CharacteristicType.On) { status.isOn }
                set(CharacteristicType.Brightness) { status.brightness }
            }
        }
    }

    override fun commitChanges() {
        if (actions.isNotEmpty()) {
            val toSend = actions.map { (key, value) -> "$key=$value" }.joinToString("&")
            scheduledFuture?.cancel(true)
            scheduledFuture = executor.schedule({
                sendRequest(HttpMethod.GET, "/light/0?$toSend") { code, _ -> if (code == 200) actions.clear() }
            }, 250, TimeUnit.MILLISECONDS)
        }
    }

}