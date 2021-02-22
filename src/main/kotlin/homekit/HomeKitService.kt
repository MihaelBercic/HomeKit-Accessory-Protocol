package homekit

import Logger
import homekit.communication.structure.data.PairingStorage
import mdns.Header
import mdns.MulticastService
import mdns.Packet
import mdns.asDatagramPacket
import mdns.records.ARecord
import mdns.records.PTRRecord
import mdns.records.SRVRecord
import mdns.records.TXTRecord
import java.net.InetAddress
import java.net.MulticastSocket
import java.security.MessageDigest
import java.util.*

/**
 * Created by Mihael Valentin Berčič
 * on 22/12/2020 at 13:44
 * using IntelliJ IDEA
 */

class HomeKitService(settings: Settings, port: Int, name: String = "HomeServer") : MulticastService("_hap._tcp.local", InetAddress.getLocalHost()) {

    init {
        Logger.trace(localhost)
    }

    private val recordName = "$name.$protocol"
    private val digest = MessageDigest.getInstance("SHA-512")
    private val answer = PTRRecord(protocol) { domain = recordName }

    override var onDiscovery: MulticastSocket.() -> Unit = {
        send(packet.asDatagramPacket)
        Logger.debug("Responding to discovery!")
    }


    private val srvRecord = SRVRecord(recordName) {
        this.port = port
        target = "$name.local"
    }

    private val addressRecord = ARecord("$name.local") { address = localhost.hostAddress }

    private val txtRecord = TXTRecord(recordName) {
        val encoder = Base64.getEncoder()
        put("id", settings.serverMAC)
        put("c#", settings.configurationNumber)
        put("sf", 1)
        put("pv", "1.1")
        put("s#", "1")
        put("ci", 2)
        put("ff", 0)
        put("md", name)
        // put("sh", encoder.encodeToString(digest.hash(*"1${settings.serverMAC}".toByteArray())))
    }


    private val packet = Packet(Header(isResponse = true)).apply {
        answerRecords.add(answer)
        additionalRecords.apply {
            add(srvRecord)
            add(addressRecord)
            add(txtRecord)
        }
    }

}