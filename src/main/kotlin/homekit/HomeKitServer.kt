package homekit

import homekit.communication.*
import homekit.pairing.PairSetup
import homekit.pairing.PairVerify
import homekit.pairing.Pairings
import homekit.pairing.TLVErrorResponse
import homekit.structure.storage.AccessoryStorage
import homekit.structure.storage.PairingStorage
import homekit.tlv.TLVError
import plugins.husqvarna.AutoMower
import plugins.shelly.dimmer.ShellyBulb
import plugins.shelly.roller.ShellyCover
import plugins.shelly.roller.ShellyRoller
import utils.*
import java.io.File
import java.net.InetAddress
import java.net.NetworkInterface
import java.net.ServerSocket


/**
 * Created by Mihael Valentin Berčič
 * on 22/12/2020 at 23:38
 * using IntelliJ IDEA
 */
class HomeKitServer(private val settings: Settings) {

    private var isRunning = true
    private var localhost: InetAddress = retrieveAddress()
    private val bridgeAddress = "http://${localhost.hostAddress}:${settings.port}"
    private val accessoryStorage: AccessoryStorage = AccessoryStorage(Bridge(bridgeAddress))
    private val pairings = readOrCompute("pairings.json") { PairingStorage() }
    private val service = HomeKitService(settings, pairings, localhost = localhost)

    init {
        File("bridge").mkdir()
    }

    fun start() {
        Logger.info("Starting the bridge... $bridgeAddress")
        if (localhost.isLoopbackAddress) throw Exception("$this is a loopback address! We can not advertise a loopback address.")

        readOrCompute("config.json") { Configuration() }.accessoryData.forEach { data ->
            val ip = data.require<String>("ip") { "IP is required for each accessory!" }
            val name = data.require<String>("name") { "Name has to be specified for each accessory!" }
            val type = data.require<String>("type") { "Type has to be specified for each accessory!" }
            val mac = data.require<String>("mac") { "An accessory MAC address has to be specified for each accessory!" }
            val additionalInformation = data["data"]?.toString() ?: ""
            val macAsNumber = mac.replace(":", "").toLong(16)

            if (macAsNumber <= 1 || accessoryStorage.contains(macAsNumber)) {
                throw Exception("Accessory ID should be larger than 1 and unique!")
            }

            val accessory = when (type) {
                "ShellyBulb" -> ShellyBulb(macAsNumber, name, ip)
                "ShellySwitch" -> ShellyRoller(macAsNumber, name, ip)
                "ShellyCover" -> ShellyCover(macAsNumber, name, ip)
                "AutoMower" -> AutoMower(macAsNumber, name, ip, additionalInformation)
                else -> throw Exception("Accessory type of $type is not supported.")
            }

            accessory.apply {
                Logger.debug("Attempting to reach $ip")
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
        Logger.info("Started our server on $localhost...")
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

    private fun retrieveAddress(): InetAddress {
        val n = NetworkInterface.networkInterfaces()
        val x = n.filter { it.name == "en0" || it.name == "eth0" || it.name == "en7" }.findFirst()
        if (x.isPresent) {
            val networkInterface = x.get()
            val address = networkInterface.inetAddresses().filter { it.isSiteLocalAddress }.findFirst()
            if (address.isPresent) return address.get()
        }
        throw Exception("Unable to fetch local address...")
    }

}