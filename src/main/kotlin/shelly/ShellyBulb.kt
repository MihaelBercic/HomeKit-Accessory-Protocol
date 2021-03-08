package shelly

import homekit.communication.structure.AppleServices
import homekit.communication.structure.CharacteristicType
import homekit.communication.structure.data.ChangeRequest
import homekit.structure.Accessory
import utils.Logger
import utils.NetworkRequestType
import utils.gson
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

class ShellyBulb(aid: Int, ip: String) : Accessory(aid, ip) {

    private val actions = mutableMapOf<String, Any>()
    private val executor = Executors.newSingleThreadScheduledExecutor()
    private var scheduledFuture: ScheduledFuture<out Any>? = null

    override
    fun setup(configurationDetails: Map<String, Any>, bridgeAddress: String) {
        addService(2, AppleServices.LightBulb).apply {
            registerInformation("Bulb", "1.0.0", "1.0.0", "Shelly", "LightBulb", "ABCDEFG") {
                Logger.info("Identifying our light bulb!")
            }

            add(CharacteristicType.On) {
                actions["turn"] = if (value == true) "on" else "off"
            }
            add(CharacteristicType.Brightness) {
                if (value != null && (value as Int) > 0) actions["brightness"] = value!!
            }
        }
    }

    override fun update() {
        sendRequest(NetworkRequestType.GET, "/light/0") { _, body ->
            val status = gson.fromJson(body, ShellyBulbStatus::class.java)
            getService(2) {
                set(CharacteristicType.On) { status.isOn }
                set(CharacteristicType.Brightness) { status.brightness }
            }
        }
    }

    override fun commitChanges(changeRequests: List<ChangeRequest>) {
        if (actions.isNotEmpty()) {
            val toSend = actions.map { (key, value) -> "$key=$value" }.joinToString("&")
            scheduledFuture?.cancel(true)
            scheduledFuture = executor.schedule({
                sendRequest(NetworkRequestType.GET, "/light/0?$toSend") { code, _ -> if (code == 200) actions.clear() }
            }, 250, TimeUnit.MILLISECONDS)
        }
    }

}