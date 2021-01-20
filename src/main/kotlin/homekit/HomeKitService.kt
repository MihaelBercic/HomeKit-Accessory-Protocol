package homekit

import generateRandomMAC
import hash
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

val serverMAC = generateRandomMAC()

class HomeKitService(name: String = "HomeServer") : MulticastService("_hap._tcp.local", InetAddress.getLocalHost()) {

    private val recordName = "$name.$protocol"
    private val digest = MessageDigest.getInstance("SHA-512")
    private val answer = PTRRecord(protocol) { domain = recordName }

    override var onDiscovery: MulticastSocket.() -> Unit = { send(discoveryPacket.asDatagramPacket) }

    private val srvRecord = SRVRecord(recordName) {
        port = 3000
        target = "$name.local"
    }

    private val addressRecord = ARecord("$name.local") { address = localhost.hostAddress }

    private val txtRecord = TXTRecord(recordName) {
        put("id", serverMAC)
        put("sf", 1)
        put("c#", 1)
        put("s#", 1)
        put("ci", 1)
        put("ff", 0)
        put("md", name)
        put("sh", Base64.getEncoder().encodeToString(digest.hash(*"1$serverMAC".toByteArray())))
    }

    private val discoveryPacket = Packet(Header(isResponse = true)).apply {
        answerRecords.add(answer)
        additionalRecords.apply {
            add(srvRecord)
            add(addressRecord)
            add(txtRecord)
        }
    }

}