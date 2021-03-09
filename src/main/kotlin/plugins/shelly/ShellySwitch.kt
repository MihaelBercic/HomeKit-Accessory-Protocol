package plugins.shelly

import homekit.structure.Accessory
import homekit.structure.data.AppleServices
import homekit.structure.data.CharacteristicType
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

    private val windowCoveringServiceId = 2

    override fun setup(configurationDetails: Map<String, Any>, bridgeAddress: String) {
        registerInformation("Shelly Switch", "1.0.0", "1.0", "Shelly", "Switch", "Sh3lly")

        addService(windowCoveringServiceId, AppleServices.WindowCovering) {
            val state = add(CharacteristicType.PositionState)
            val position = add(CharacteristicType.CurrentPosition)
            val target = add(CharacteristicType.TargetPosition) {
                value?.apply {
                    actions["go"] = "to_pos"
                    actions["roller_pos"] = this
                }
            }

            val query = "${target.iid},${state.iid},${position.iid}"
            val notificationStopUrl = "/settings/roller/0?roller_stop_url=$bridgeAddress/event?$aid:$query"
            sendRequest(NetworkRequestType.GET, notificationStopUrl)
        }
    }

    override fun update() {
        sendRequest(NetworkRequestType.GET, "/roller/0") { _, body ->
            getService(2) {
                val data = gson.fromJson(body, ShellySwitchStatus::class.java)
                set(CharacteristicType.CurrentPosition) { data.position }
                set(CharacteristicType.TargetPosition) { data.position }
            }
        }
    }

    override fun commitChanges() {
        val query = actions.map { "${it.key}=${it.value}" }.joinToString("&")
        scheduledFuture?.cancel(true)
        scheduledFuture = executor.schedule({ URL("http://$ip/roller/0?$query").readText() }, 1000, TimeUnit.MILLISECONDS)
        actions.clear()
    }

}