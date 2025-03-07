package plugins.shelly.roller

import homekit.structure.Accessory
import homekit.structure.data.CharacteristicType
import homekit.structure.data.ServiceType
import utils.HttpMethod
import utils.gson
import java.net.URL
import java.net.URLEncoder
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

class ShellyCover(aid: Long, name: String, ip: String) : Accessory(aid, name, ip) {

    private val actions = mutableMapOf<String, Any>()
    private val executor = Executors.newSingleThreadScheduledExecutor()
    private var scheduledFuture: ScheduledFuture<out Any>? = null

    private val windowCoveringServiceId = 2L

    override fun setup(configurationDetails: Map<String, Any>, bridgeAddress: String) {
        registerInformation("1.0.0", "1.0", "Shelly", "Switch", "Sh3lly")
        // sendRequest(HttpMethod.GET, "/ota?update=true")

        addService(windowCoveringServiceId, ServiceType.WindowCovering) {
            val state = add(CharacteristicType.PositionState)
            val position = add(CharacteristicType.CurrentPosition)
            val target = add(CharacteristicType.TargetPosition) {
                val newValue = value as Int
                val currentValue = position.value as Int

                actions["go"] = "to_pos"
                actions["roller_pos"] = newValue
                state.value = when {
                    newValue < currentValue -> 0
                    newValue > currentValue -> 1
                    else -> 2
                }
            }

            sendRequest(HttpMethod.GET, "/rpc/Webhook.DeleteAll")
            val query = "${target.iid},${state.iid},${position.iid}"
            val urls = arrayOf("\"$bridgeAddress/event?$aid:$query\"").map { URLEncoder.encode(it, Charsets.UTF_8) }
            val notificationStopUrl = "/rpc/Webhook.Create?event=cover.stopped&cid=0&enable=true&urls=[${urls.joinToString(",")}]"
            sendRequest(HttpMethod.GET, notificationStopUrl)
        }
    }

    override fun update() {
        sendRequest(HttpMethod.GET, "/roller/0/") { _, body ->
            getService(2) {
                val data = gson.fromJson(body, ShellyCoverStatus::class.java)
                set(CharacteristicType.CurrentPosition) { data.position }
                set(CharacteristicType.TargetPosition) { data.position }
                set(CharacteristicType.PositionState) {
                    when (data.state) {
                        PositionStates.Stopped -> 2
                        PositionStates.Closing -> 0
                        PositionStates.Opening -> 1
                    }
                }
            }
        }
    }

    override fun commitChanges() {
        if (actions.isNotEmpty()) {
            val query = actions.map { "${it.key}=${it.value}" }.joinToString("&")
            scheduledFuture?.cancel(true)
            scheduledFuture = executor.schedule({ URL("http://$ip/roller/0?$query").readText() }, 1000, TimeUnit.MILLISECONDS)
            actions.clear()
        }
    }

}