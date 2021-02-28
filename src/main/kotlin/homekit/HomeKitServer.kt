package homekit

import Logger
import com.google.gson.annotations.SerializedName
import gson
import homekit.communication.*
import homekit.communication.structure.*
import homekit.communication.structure.data.*
import homekit.pairing.PairSetup
import homekit.pairing.PairVerify
import homekit.pairing.Pairings
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
    val liveSessions = mutableListOf<Session>()

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
                val accessoryType = data getString "type"
                val accessory = when (accessoryType) {
                    "Light" -> Bulb(index + 2)
                    "Switch" -> Switch(index + 2)
                    else -> throw Exception("Accessory type of $accessoryType is not supported.")
                }
                accessory.setup(data)
                accessoryStorage.addAccessory(accessory)
            }
        }

        Thread {
            ServerSocket(port).apply {
                soTimeout = 0
                Logger.info("Started our server...")
                while (isRunning) {
                    val newSocket = accept()
                    newSocket.soTimeout = 0
                    Thread { Session(newSocket, this@HomeKitServer) }.start()
                }
            }
        }.start()
    }

    // TODO A lot of work
    fun handle(httpRequest: HttpRequest, session: Session): Response? {
        val headers = httpRequest.headers
        val method = headers.httpMethod
        val path = headers.path
        val query = headers.query
        val response = when {
            path == "/pair-setup" && method == HttpMethod.POST -> PairSetup.handleRequest(settings, pairings, session, httpRequest.content)
            path == "/pair-verify" && method == HttpMethod.POST -> PairVerify.handleRequest(settings, pairings, session, httpRequest.content)
            path == "/accessories" && method == HttpMethod.GET -> HttpResponse(data = *gson.toJson(this.accessoryStorage).apply { println(this) }.toByteArray())
            path == "/characteristics" && method == HttpMethod.PUT -> {
                val body = String(httpRequest.content)
                val changeRequests = gson.fromJson(body, ChangeRequests::class.java)
                val responses = mutableListOf<String>()
                val characteristics = changeRequests.characteristics.groupBy { it.aid }

                characteristics.forEach { (aid, characteristics) ->
                    val accessory = accessoryStorage[aid]
                    characteristics.forEach {
                        val iid = it.iid
                        val characteristic = accessory[iid]
                        val status = when {
                            it.events != null -> {
                                if (it.events && characteristic.supportsEvents) {
                                    characteristic.ev = true
                                    0
                                } else -70406
                            }
                            it.value != null -> {
                                characteristic.value = it.value
                                0
                            }
                            else -> 0
                        }
                        Logger.info(it)
                        responses.add("{\"aid\": $aid, \"iid\":$iid, \"status\": $status}")
                    }
                    accessory.commitChanges(characteristics)
                }
                HttpResponse(207, data = *"{\"characteristics\":[${responses.joinToString(",")}]}".toByteArray())
            }
            path == "/characteristics" && method == HttpMethod.GET && query != null -> {
                val querySplit = query.split("&")
                val ids = querySplit[0]
                    .replace("id=", "")
                    .split(",")
                    .map { it.split(".").let { split -> split[0].toInt() to split[1].toInt() } }
                    .groupBy { it.first }

                val toReturn = mutableListOf<CharacteristicResponse>()
                ids.forEach { (aid, pairs) ->
                    val accessory = accessoryStorage[aid]
                    pairs.forEach { (_, iid) ->
                        val characteristic = accessory[iid]
                        toReturn.add(CharacteristicResponse(aid, iid, characteristic.value))
                    }
                }
                HttpResponse(data = *("{ \"characteristics\" : ${gson.toJson(toReturn)} }").toByteArray())
            }
            path == "/pairings" && method == HttpMethod.POST -> Pairings.handleRequest(session, pairings, httpRequest.content)
            path == "/event" && method == HttpMethod.GET -> {
                if (query == null) throw Exception("Query missing for event!")
                session.shouldClose = true
                session.sendMessage(HttpResponse(200), false)
                // TODO multiple characteristics!
                val split = query.replace("?characteristics=", "").split(":")
                val aid = split[0].toInt()
                val accessory = accessoryStorage[aid]
                val iids = split[1].split(",").map {
                    val characteristic = accessory[it.toInt()]
                    CharacteristicResponse(aid, characteristic.iid, characteristic.value)
                }
                val httpResponse = HttpResponse(type = ResponseType.Event, data = *gson.toJson(CharacteristicsResponse(iids)).toByteArray())
                liveSessions.forEach {
                    if (it.isSecure) {
                        Logger.info("Sending event to an active session!")
                        println(String(httpResponse.data))
                        it.sendMessage(httpResponse)
                    }
                }
                null
            }
            else -> TLVErrorResponse(2, TLVError.Unknown)
        }
        return response
    }

}

class Configuration {

    @SerializedName("accessories")
    val accessoryData: List<Map<String, Any>> = emptyList()

    internal infix fun Map<String, Any>.getString(name: String) = get(name) as? String ?: throw Exception("Map does not contain the key.")
    private infix fun Map<String, Any>.getInteger(name: String) = get(name) as? Int ?: throw Exception("Map does not contain the key.")

}