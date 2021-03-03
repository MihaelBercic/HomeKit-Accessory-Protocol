package homekit

import Logger
import mdns.Header
import mdns.MulticastService
import mdns.Packet
import mdns.asDatagramPacket
import mdns.records.ARecord
import mdns.records.PTRRecord
import mdns.records.SRVRecord
import mdns.records.TXTRecord
import mdns.records.structure.RecordType
import java.lang.Thread.sleep
import java.net.DatagramPacket
import java.net.InetAddress
import java.net.MulticastSocket
import kotlin.random.Random

/**
 * Created by Mihael Valentin Berčič
 * on 22/12/2020 at 13:44
 * using IntelliJ IDEA
 */

class HomeKitService(settings: Settings, name: String = "HomeServer") : MulticastService("_hap._tcp.local", InetAddress.getLocalHost()) {

    init {
        Logger.trace(localhost)
    }

    private val recordName = "$name.$protocol"
    private val targetName = "$name.local"
    private val tcpAnswer = PTRRecord(protocol) { domain = recordName }

    override var responseCondition: Packet.() -> Boolean = {
        !header.isResponse
                && queryRecords.any { it.label == protocol || it.label == recordName }
                && answerRecords.none { it.label == protocol || it.label == recordName || it.label == targetName }
    }

    override var respondWith: (MulticastSocket, DatagramPacket, Packet) -> Unit = { socket, datagramPacket, packet ->
        val desiredQueries = packet.queryRecords.filter { it.label == protocol || it.label == recordName }
        Logger.info("[${datagramPacket.socketAddress}] is asking for ${desiredQueries.map { "[${if (it.hasProperty) "Unicast" else "Multicast"}] ${it.label}" }} with answers ${packet.answerRecords.map { "${it.type} ${it.label}" }}")
        val includePointer = desiredQueries.any { it.type == RecordType.PTR }
        val includeTXT = desiredQueries.any { it.type == RecordType.TXT }
        val includeSRV = desiredQueries.any { it.type == RecordType.SRV }
        val isUnicast = desiredQueries.any { it.hasProperty }
        val respondingPacket = Packet(Header(isResponse = true)).apply {
            if (includePointer) {
                answerRecords.add(tcpAnswer)
                additionalRecords.apply {
                    add(srvRecord)
                    add(addressRecord)
                    add(txtRecord)
                }
            }
            if (includeTXT) answerRecords.add(txtRecord)
            if (includeSRV) answerRecords.add(srvRecord)
        }
        val newDatagramPacket = respondingPacket.asDatagramPacket
        datagramPacket.data = newDatagramPacket.data
        if (!isUnicast) sleep(Random.nextLong(100, 300))
        socket.send(if (isUnicast) datagramPacket else newDatagramPacket)
    }

    private val srvRecord = SRVRecord(recordName) {
        port = settings.port
        target = targetName
    }

    private val addressRecord = ARecord("$name.local") { address = localhost.hostAddress }

    private val txtRecord = TXTRecord(recordName) {
        put("c#", settings.configurationNumber)
        put("id", settings.serverMAC)
        put("md", name)
        put("pv", "1.1")
        put("s#", "1")
        put("sf", 0x00) // TODO update when paired / unpaired
        put("ci", 2)
    }


    override val wakeUpPacket: Packet = Packet(Header(isResponse = true)).apply {
        answerRecords.add(tcpAnswer)
        additionalRecords.apply {
            add(srvRecord)
            add(addressRecord)
            add(txtRecord)
        }
    }


}