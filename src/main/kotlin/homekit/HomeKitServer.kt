package homekit

import homekit.communication.*
import homekit.communication.LiveSessions.subscribeFor
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
import utils.gson
import utils.readOrCompute
import java.io.File
import java.net.InetAddress
import java.net.ServerSocket


/**
 * Created by Mihael Valentin Berčič
 * on 22/12/2020 at 23:38
 * using IntelliJ IDEA
 */
class HomeKitServer(private val settings: Settings) {

    private val localhost = InetAddress.getLocalHost()
    private val bridgeAddress = "http://${localhost.hostAddress}:${settings.port}"
    private var isRunning = true
    private val accessoryStorage: AccessoryStorage = AccessoryStorage(Bridge(bridgeAddress))
    private val service = HomeKitService(settings)
    private val pairings = readOrCompute("pairings.json") { PairingStorage() }

    init {
        File("bridge").mkdir()
    }

    fun start() {
        if (localhost.isLoopbackAddress) throw Exception("$this is a loopback address! We can not advertise a loopback address.")

        readOrCompute("config.json") { Configuration() }.accessoryData.forEach { data ->
            val ip = data.require<String>("ip") { "IP is required for each accessory!" }
            val type = data.require<String>("type") { "Accessory type has to be specified for each accessory!" }
            val id = data.require<Int>("id") { "A unique Accessory ID (aid) has to be specified for each accessory!" }

            if (id <= 1 || accessoryStorage.contains(id)) {
                throw Exception("Accessory ID should be larger than 1 and unique!")
            }

            val accessory = when (type) {
                "Light" -> ShellyBulb(id, ip)
                "ShellySwitch" -> ShellySwitch(id, ip)
                else -> throw Exception("Accessory type of $type is not supported.")
            }

            accessory.apply {
                setup(data, bridgeAddress)
                update()
                accessoryStorage.addAccessory(this)
                Logger.debug("Successfully registered $ip with aid $id and type $type.")
            }
        }
        Thread {
            ServerSocket(settings.port).apply {
                soTimeout = 0
                while (isRunning) {
                    val newSocket = accept().apply {
                        soTimeout = 0
                        tcpNoDelay = true
                    }
                    Thread { Session(newSocket, this@HomeKitServer) }.start()
                }
            }
        }.start()
        service.startAdvertising()
        Logger.info("Started our server...")
    }

    fun handle(httpRequest: HttpRequest, session: Session): Response {
        val headers = httpRequest.headers
        val method = headers.httpMethod
        val path = headers.path
        val query = headers.query
        Logger.apply { debug("$green${session.remoteSocketAddress}$reset [$magenta$method$reset] $path $yellow$query$reset") }
        return when {
            path == "/pair-setup" && method == HttpMethod.POST -> PairSetup.handleRequest(settings, pairings, session, httpRequest.content)
            path == "/pair-verify" && method == HttpMethod.POST -> PairVerify.handleRequest(settings, pairings, session, httpRequest.content)
            path == "/pairings" && method == HttpMethod.POST -> Pairings.handleRequest(session, service, pairings, httpRequest.content)
            path == "/accessories" && method == HttpMethod.GET -> accessoryStorage.createHttpResponse()
            path == "/characteristics" && method == HttpMethod.GET && query != null -> Characteristics.retrieve(query, accessoryStorage)
            path == "/event" && method == HttpMethod.GET -> Events.handleEvents(accessoryStorage, query, session)
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
                            else -> {
                                characteristic.value = it.value
                                0
                            }
                        }
                        responses.add("{\"aid\": $aid, \"iid\":$iid, \"status\": $status}")
                    }
                    accessory.commitChanges(characteristics)
                }
                HttpResponse(207, data = "{\"characteristics\":[${responses.joinToString(",")}]}".toByteArray())
            }
            else -> TLVErrorResponse(2, TLVError.Unknown)
        }
    }

    private inline fun <reified T> Map<String, Any>.require(key: String, message: () -> String): T {
        val value = this[key] ?: throw Exception(message())
        return when (T::class) {
            Int::class -> (value as Double).toInt() as T
            else -> value as T
        }
    }
}