package homekit

import Logger
import com.google.gson.annotations.SerializedName
import gson
import homekit.communication.*
import homekit.communication.structure.*
import homekit.communication.structure.data.*
import homekit.pairing.PairSetup
import homekit.pairing.PairVerify
import homekit.pairing.TLVErrorResponse
import homekit.tlv.structure.TLVError
import readOrCompute
import java.net.ServerSocket

/**
 * Created by Mihael Valentin Berčič
 * on 22/12/2020 at 23:38
 * using IntelliJ IDEA
 */
class HomeKitServer(val settings: Settings) {

    private var isRunning = true
    private val accessoryStorage: AccessoryStorage = AccessoryStorage()
    private val pairings = readOrCompute("pairings.json") { PairingStorage() }
    private val networkNames = mutableListOf<String>()
    private val deviceMap = hashMapOf<Int, Device>()

    fun start(port: Int) {
        settings.apply {
            configurationNumber++
            save()
        }
        Bridge().apply {
            setup(emptyMap())
            accessoryStorage.addAccessory(this)
        }
        readOrCompute("config.json") { Configuration() }.apply {
            accessoryData.forEachIndexed { index, data ->
                val networkName = data getString "networkName"
                val accessoryType = data getString "type"
                val accessory = when (accessoryType) {
                    "Light" -> Bulb(index + 2)
                    else -> throw Exception("Accessory type of $accessoryType is not supported.")
                }
                accessory.setup(data)
                deviceMap[accessory.aid] = Device("TODO:3000/20200", networkName)
                accessoryStorage.addAccessory(accessory)
            }
        }

        Thread {
            ServerSocket(port).apply {
                soTimeout = 0
                Logger.info("Started our server...")
                while (isRunning) {
                    val newSocket = accept()
                    Thread { Session(newSocket, this@HomeKitServer) }.start()
                }
            }
        }.start()
    }

    // TODO A lot of work
    fun handle(httpRequest: HttpRequest, session: Session): Response {
        val headers = httpRequest.headers
        val method = headers.httpMethod
        val path = headers.path
        val query = headers.query
        val response = when {
            path == "/pair-setup" && method == HttpMethod.POST -> PairSetup.handleRequest(settings, pairings, session, httpRequest.content)
            path == "/pair-verify" && method == HttpMethod.POST -> PairVerify.handleRequest(settings, pairings, session, httpRequest.content)
            path == "/accessories" && method == HttpMethod.GET -> HttpResponse(data = *gson.toJson(this.accessoryStorage).toByteArray())
            path == "/characteristics" && method == HttpMethod.PUT -> {
                val body = String(httpRequest.content)
                val changeRequests = gson.fromJson(body, ChangeRequests::class.java)
                val responses = mutableListOf<String>()
                val characteristics = changeRequests.characteristics.groupBy { it.aid }

                characteristics.forEach { (aid, characteristics) ->
                    val device = deviceMap[aid] ?: throw Exception("No device for $aid")
                    val accessory = accessoryStorage[aid] ?: throw Exception("No accessory with id $aid")
                    characteristics.forEach {
                        val iid = it.iid
                        val status = if (it.events) -70406 else 0
                        val characteristic = accessory[iid]
                        if (it.value != null) characteristic.value = it.value
                        responses.add("{\"aid\": $aid, \"iid\":$iid, \"status\": $status}")
                    }
                    accessory.commitChanges(device, characteristics)
                }
                HttpResponse(207, data = *"{\"characteristics\":[${responses.joinToString(",")}]}".toByteArray())
            }
            path == "/characteristics" && method == HttpMethod.GET && query != null -> {
                val querySplit = query.split("&")
                val ids = querySplit[0].replace("id=", "").split(",")
                val toReturn = mutableListOf<CharacteristicResponse>()
                ids.forEach { pair ->
                    val split = pair.split(".")
                    val aid = split[0].toInt()
                    val iid = split[1].toInt()
                    val accessory = accessoryStorage[aid] ?: throw java.lang.Exception("Accessory does not exist...")
                    accessory.update()
                    val characteristic = accessory[iid]
                    val characteristicResponse = CharacteristicResponse(aid, iid).apply {
                        // bunch of ifs...
                        value = characteristic.value
                    }
                    toReturn.add(characteristicResponse)
                }

                HttpResponse(data = *("{ \"characteristics\" : ${gson.toJson(toReturn)} }").toByteArray())
            }
            else -> TLVErrorResponse(2, TLVError.Unknown)
        }
        return response
    }

    private fun updateAddresses() {

    }
}


private val usedIds = mutableListOf<Int>()


class Configuration {

    @SerializedName("accessories")
    val accessoryData: List<Map<String, Any>> = emptyList()

    internal infix fun Map<String, Any>.getString(name: String) = get(name) as? String ?: throw Exception("Map does not contain the key.")
    private infix fun Map<String, Any>.getInteger(name: String) = get(name) as? Int ?: throw Exception("Map does not contain the key.")

}

data class Device(val ip: String, val networkName: String) {
    fun sendMessage(message: Any) {

    }
}