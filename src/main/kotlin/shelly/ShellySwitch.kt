package shelly

import homekit.communication.structure.AppleServices
import homekit.communication.structure.CharacteristicType
import homekit.communication.structure.data.ChangeRequest
import homekit.structure.Accessory
import utils.Logger
import utils.NetworkRequestType
import utils.gson
import java.net.URL
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

class ShellySwitch(aid: Int, ip: String) : Accessory(aid, ip) {

    private val actions = mutableMapOf<String, Any>()
    private val executor = Executors.newSingleThreadScheduledExecutor()
    private var scheduledFuture: ScheduledFuture<out Any>? = null

    override fun setup(configurationDetails: Map<String, Any>) {
        registerInformation("Shelly Switch", "1.0.0", "1.0", "Shelly", "Switch", "Sh2lly") {
            Logger.trace("Identifying shelly switch with id: $aid")
        }

        service(2, AppleServices.WindowCovering) {
            sendRequest(NetworkRequestType.GET, "/roller/0") { _, body ->
                val switchStatus = gson.fromJson(body, ShellySwitchStatus::class.java)
                val state = addCharacteristic(CharacteristicType.PositionState, supportsEvents = true)
                val currentPosition = addCharacteristic(CharacteristicType.CurrentPosition, switchStatus.position, supportsEvents = true)
                val targetPosition = addCharacteristic(CharacteristicType.TargetPosition, switchStatus.position) {
                    value?.apply {
                        currentPosition.value = this
                        actions["go"] = "to_pos"
                        actions["roller_pos"] = this
                    }
                }

                addCharacteristic(CharacteristicType.HoldPosition) {
                    Logger.info("Sending stop request because hold position has changed!")
                    sendRequest(NetworkRequestType.GET, "/roller/0?go=stop")
                }
                sendRequest(NetworkRequestType.GET, "/settings/roller/0?roller_stop_url=http://192.168.1.20:3000/event?characteristics=$aid:${currentPosition.iid},${state.iid},${targetPosition.iid}")
            }
        }

    }

    override fun commitChanges(changeRequests: List<ChangeRequest>) {
        val query = actions.map { "${it.key}=${it.value}" }.joinToString("&")
        scheduledFuture?.cancel(true)
        scheduledFuture = executor.schedule({ URL("http://$ip/roller/0?$query").readText() }, 1000, TimeUnit.MILLISECONDS)
        actions.clear()
    }

}