package homekit

import homekit.communication.*
import homekit.communication.LiveSessions.subscribeFor
import homekit.communication.LiveSessions.subscribedSessions
import homekit.communication.LiveSessions.unsubscribeFrom
import homekit.communication.structure.*
import homekit.communication.structure.data.*
import homekit.pairing.PairSetup
import homekit.pairing.PairVerify
import homekit.pairing.Pairings
import homekit.pairing.TLVErrorResponse
import homekit.structure.Bridge
import homekit.tlv.TLVError
import shelly.ShellyBulb
import shelly.ShellySwitch
import utils.Logger
import utils.appleGson
import utils.gson
import utils.readOrCompute
import java.net.ServerSocket

/**
 * Created by Mihael Valentin Berčič
 * on 22/12/2020 at 23:38
 * using IntelliJ IDEA
 */
class HomeKitServer(val settings: Settings) {

    private var isRunning = true
    private val accessoryStorage: AccessoryStorage = AccessoryStorage(Bridge())
    private val pairings = readOrCompute("pairings.json") { PairingStorage() }

    fun start() {
        settings.apply {
            configurationNumber++
            save()
        }
        readOrCompute("config.json") { Configuration() }.apply {
            accessoryData.forEach { data ->
                val accessoryIP = data getString "ip"
                val accessoryType = data getString "type"
                val aid = data getInteger "id"
                if (aid <= 1 || accessoryStorage.contains(aid)) throw Exception("Accessory ID should be larger than 1 and unique!")
                val accessory = when (accessoryType) {
                    "Light" -> ShellyBulb(aid, accessoryIP)
                    "ShellySwitch" -> ShellySwitch(aid, accessoryIP)
                    else -> throw Exception("Accessory type of $accessoryType is not supported.")
                }
                accessory.setup(data)
                accessoryStorage.addAccessory(accessory)
                Logger.debug("Successfully registered $accessoryIP with aid $aid and type $accessoryType.")
            }
        }
        Thread {
            ServerSocket(settings.port).apply {
                soTimeout = 0
                Logger.info("Started our server...")
                while (isRunning) {
                    val newSocket = accept().apply {
                        soTimeout = 0
                        tcpNoDelay = true
                    }
                    Thread { Session(newSocket, this@HomeKitServer) }.start()
                }
            }
        }.start()
    }

    fun handle(httpRequest: HttpRequest, session: Session): Response? {
        val headers = httpRequest.headers
        val method = headers.httpMethod
        val path = headers.path
        val query = headers.query
        Logger.apply {
            debug("$green${session.remoteSocketAddress}$reset [$magenta$method$reset] $path $yellow$query$reset")
        }
        val response = when {
            path == "/pair-setup" && method == HttpMethod.POST -> PairSetup.handleRequest(settings, pairings, session, httpRequest.content)
            path == "/pair-verify" && method == HttpMethod.POST -> PairVerify.handleRequest(settings, pairings, session, httpRequest.content)
            path == "/pairings" && method == HttpMethod.POST -> Pairings.handleRequest(session, pairings, httpRequest.content)
            path == "/accessories" && method == HttpMethod.GET -> accessoryStorage.createHttpResponse()
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
                                if (it.events) session.subscribeFor(characteristic) else session.unsubscribeFrom(characteristic)
                                0
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
                HttpResponse(207, data = "{\"characteristics\":[${responses.joinToString(",")}]}".toByteArray())
            }
            path == "/characteristics" && method == HttpMethod.GET && query != null -> {
                val querySplit = query.replace("id=", "").split("&")
                val ids = querySplit[0]
                    .replace("id=", "")
                    .split(",")
                    .map { it.split(".").let { split -> split[0].toInt() to split[1].toLong() } }
                    .groupBy { it.first }

                val toReturn = mutableListOf<CharacteristicResponse>()
                ids.forEach { (aid, pairs) ->
                    val accessory = accessoryStorage[aid]
                    pairs.forEach { (_, iid) ->
                        val characteristic = accessory[iid]
                        toReturn.add(CharacteristicResponse(aid, iid, characteristic.value, status = StatusCodes.Success.value))
                    }
                }
                HttpResponse(207, data = ("{ \"characteristics\" : ${appleGson.toJson(toReturn)} }").toByteArray())
            }
            path == "/event" && method == HttpMethod.GET -> {
                if (query == null) throw Exception("Query missing for event!")
                session.shouldClose = true
                session.sendMessage(HttpResponse(200), false)
                // TODO multiple characteristics!
                val split = query.replace("characteristics=", "").split(":")
                val aid = split[0].toInt()
                val accessory = accessoryStorage[aid]
                val sessionsToSendTo = mutableSetOf<Session>()
                val iids = split[1].split(",").map {
                    val characteristic = accessory[it.toLong()]
                    sessionsToSendTo.addAll(characteristic.subscribedSessions)
                    CharacteristicResponse(aid, characteristic.iid, characteristic.value)
                }
                val httpResponse = HttpResponse(type = ResponseType.Event, data = appleGson.toJson(CharacteristicsResponse(iids)).toByteArray())

                sessionsToSendTo.forEach {
                    Logger.info("Sending event to [${it.currentController.identifier}]!")
                    it.sendMessage(httpResponse)
                }
                null
            }
            else -> TLVErrorResponse(2, TLVError.Unknown)
        }
        return response
    }

}