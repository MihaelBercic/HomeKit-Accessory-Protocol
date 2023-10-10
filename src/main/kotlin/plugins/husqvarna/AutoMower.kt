package plugins.husqvarna

import homekit.structure.Accessory
import homekit.structure.data.CharacteristicType
import homekit.structure.data.ServiceType
import plugins.husqvarna.structure.*
import utils.*
import java.net.URI
import java.net.http.HttpRequest
import java.net.http.HttpRequest.BodyPublishers
import java.net.http.HttpResponse
import java.net.http.HttpResponse.BodyHandlers
import java.util.Timer
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit
import kotlin.concurrent.fixedRateTimer
import kotlin.concurrent.schedule

class AutoMower(aid: Long, name: String, ip: String, private val additionalData: String) : Accessory(aid, name) {

    private data class DurationAttributes(val duration: Int = 300)
    private data class ActionRequest(val type: String, val attributes: DurationAttributes?)
    private data class ApiRequest(val data: ActionRequest)

    private val mowerData: AdditionalAutoMowerDataConfiguration = gson.fromJson(additionalData, AdditionalAutoMowerDataConfiguration::class.java)
    private val mowerEndpoint = "https://api.amc.husqvarna.dev/v1/mowers/${mowerData.id}"

    private var nextRequest: ApiRequest? = null
    private var cachedAuthDetails: HusqvarnaAuth? = null
    private var activeIID: Long = 0

    private fun fetchAuthenticationDetails(): HusqvarnaAuth {
        val currentDetails = cachedAuthDetails
        val hasToFetch = currentDetails == null || System.currentTimeMillis() >= currentDetails.createdAt + currentDetails.expiresIn * 1000
        if (!hasToFetch) return currentDetails!!
        val formEncoded = "grant_type=client_credentials&client_id=${mowerData.clientId}&client_secret=${mowerData.clientSecret}"
        val request = HttpRequest.newBuilder(URI("https://api.authentication.husqvarnagroup.dev/v1/oauth2/token"))
            .headers("Content-Type", "application/x-www-form-urlencoded")
            .POST(HttpRequest.BodyPublishers.ofString(formEncoded)).build()
        val response = httpClient.send(request, BodyHandlers.ofString());
        val details = gson.fromJson(response.body(), HusqvarnaAuth::class.java).apply {
            createdAt = System.currentTimeMillis()
        }
        cachedAuthDetails = details
        return details
    }

    private fun getMowerDetails(): CompletableFuture<HttpResponse<String>> {
        val request = HttpRequest.newBuilder(URI(mowerEndpoint)).GET().apply(this::authenticationHeaders).build()
        return httpClient.sendAsync(request, BodyHandlers.ofString())
    }

    override fun setup(configurationDetails: Map<String, Any>, bridgeAddress: String) {
        registerInformation("1.0.0", "1.0.0", "Husqvarna", "AutoMower", "IDFKDSAD")
        addService(2, ServiceType.Battery) {
            val batteryLevel = add(CharacteristicType.BatteryLevel)
            val chargingState = add(CharacteristicType.ChargingState)
            val lowBattery = add(CharacteristicType.LowBattery)

            val query = "${batteryLevel.iid},${chargingState.iid},${lowBattery.iid}"
            Logger.info("AutoMower url: $bridgeAddress/event?$aid:$query")
        }
        addService(3, ServiceType.Fan) {
            val activeCharacteristic = add(CharacteristicType.Active) {
                Logger.info("Active characteristic value $value")
                if (value != null) {
                    val actionType = if (value as? Int == 1) "Start" else "ParkUntilNextSchedule"
                    val attributes = if (actionType == "Start") DurationAttributes() else null
                    val actionRequest = ActionRequest(actionType, attributes)
                    nextRequest = ApiRequest(actionRequest)
                }
            }
            activeIID = activeCharacteristic.iid
            Logger.info("IID: ${activeCharacteristic.iid}")
        }
        val minutes = TimeUnit.MINUTES.toMillis(15)
        fixedRateTimer("AutoMower Update Timer", period = minutes, initialDelay = minutes) {
            update()
        }
    }

    private fun fetchCloudStatus() {
        getMowerDetails().thenAccept { response ->
            val details = gson.fromJson(response.body(), AutoMowerDataResponse::class.java)
            val mowerData = details.data.attributes
            val activity = mowerData.mower.activity
            Logger.info("Updated information: $mowerData")
            getService(2) {
                set(CharacteristicType.BatteryLevel) { mowerData.battery.batteryPercent }
                set(CharacteristicType.LowBattery) { if (mowerData.battery.batteryPercent < 20) 1 else 0 }
                set(CharacteristicType.ChargingState) { if (activity == "CHARGING" || activity == "PARKED_IN_CS") 1 else 0 }
            }
            getService(3) {
                Logger.error("Is mower active? ${activity == "MOWING" || activity == "LEAVING"}")
                set(CharacteristicType.Active) { if (activity == "MOWING" || activity == "LEAVING") 1 else 0 }
            }
        }
    }

    override fun update() {
        fetchCloudStatus()
    }

    override fun commitChanges() {
        fetchCloudStatus()
        if (nextRequest == null) return
        val request = HttpRequest.newBuilder(URI("$mowerEndpoint/actions".apply(Logger::info))).POST(BodyPublishers.ofString(gson.toJson(nextRequest))).apply(this::authenticationHeaders).build()
        val response = httpClient.sendAsync(request, BodyHandlers.ofString())
        Timer().schedule(30 * 1000) {
            update()
        }
        nextRequest = null
    }

    private fun authenticationHeaders(builder: HttpRequest.Builder): HttpRequest.Builder {
        val authData = fetchAuthenticationDetails()
        return builder.apply {
            header("Accept", "application/vnd.api+json")
            header("Content-Type", "application/vnd.api+json")
            header("X-Api-Key", mowerData.clientId)
            header("Authorization", "Bearer ${authData.accessToken}")
            header("Authorization-Provider", "husqvarna")
        }
    }
}