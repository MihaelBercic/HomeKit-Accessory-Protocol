package homekit

import homekit.communication.*
import homekit.pairing.PairSetup
import homekit.pairing.PairVerify
import homekit.pairing.Pairings
import homekit.pairing.TLVErrorResponse
import homekit.structure.*
import homekit.structure.data.*
import homekit.structure.storage.AccessoryStorage
import homekit.structure.storage.PairingStorage
import homekit.tlv.TLVError
import plugins.shelly.dimmer.ShellyBulb
import plugins.shelly.roller.ShellyRoller
import utils.HttpMethod
import utils.Logger
import utils.readOrCompute
import utils.require
import java.io.File
import java.net.InetAddress
import java.net.ServerSocket


/**
 * Created by Mihael Valentin Berčič
 * on 22/12/2020 at 23:38
 * using IntelliJ IDEA
 */
class HomeKitServer(private val settings: Settings) {

    private var isRunning = true
    private val localhost = InetAddress.getLocalHost()
    private val bridgeAddress = "http://${localhost.hostAddress}:${settings.port}"
    private val accessoryStorage: AccessoryStorage = AccessoryStorage(Bridge(bridgeAddress))
    private val pairings = readOrCompute("pairings.json") { PairingStorage() }
    private val service = HomeKitService(settings, pairings)

    init {
        File("bridge").mkdir()
    }

    fun start() {
        if (localhost.isLoopbackAddress) throw Exception("$this is a loopback address! We can not advertise a loopback address.")

        readOrCompute("config.json") { Configuration() }.accessoryData.forEach { data ->
            val ip = data.require<String>("ip") { "IP is required for each accessory!" }
            val name = data.require<String>("name") { "Name has to be specified for each accessory!" }
            val type = data.require<String>("type") { "Type has to be specified for each accessory!" }
            val mac = data.require<String>("mac") { "An accessory MAC address has to be specified for each accessory!" }
            val macAsNumber = mac.replace(":", "").toLong(16)

            if (macAsNumber <= 1 || accessoryStorage.contains(macAsNumber)) {
                throw Exception("Accessory ID should be larger than 1 and unique!")
            }

            val accessory = when (type) {
                "ShellyBulb" -> ShellyBulb(macAsNumber, name, ip)
                "ShellySwitch" -> ShellyRoller(macAsNumber, name, ip)
                else -> throw Exception("Accessory type of $type is not supported.")
            }

            accessory.apply {
                setup(data, bridgeAddress)
                update()
                accessoryStorage.addAccessory(this)
                Logger.debug("Successfully registered ${ip.padEnd(13)} with aid $macAsNumber and type $type.")
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
        settings.increaseConfiguration() // TODO modify
        service.startAdvertising()
        Logger.info("Started our server...")
        Logger.info(accessoryStorage.asJson)
    }

    fun handle(httpRequest: HttpRequest, session: Session): Response {
        val headers = httpRequest.headers
        val method = headers.httpMethod
        val path = headers.path
        val query = headers.query
        Logger.apply { debug("$green${session.remoteSocketAddress.toString().padEnd(20)}$reset [$magenta$method$reset] $path $yellow$query$reset") }
        return when {
            path == "/pair-setup" && method == HttpMethod.POST -> PairSetup.handleRequest(settings, pairings, session, httpRequest.content)
            path == "/pair-verify" && method == HttpMethod.POST -> PairVerify.handleRequest(settings, pairings, session, httpRequest.content)
            path == "/pairings" && method == HttpMethod.POST -> Pairings.handleRequest(session, service, pairings, httpRequest.content)
            path == "/accessories" && method == HttpMethod.GET -> accessoryStorage.createHttpResponse()
            path == "/characteristics" && method == HttpMethod.PUT -> Characteristics.resolveChangeRequests(String(httpRequest.content), accessoryStorage, session)
            path == "/characteristics" && method == HttpMethod.GET && query != null -> Characteristics.retrieve(query, accessoryStorage)
            path == "/event" && method == HttpMethod.GET -> Events.handleEvents(accessoryStorage, query, session)
            else -> TLVErrorResponse(2, TLVError.Unknown)
        }
    }

}