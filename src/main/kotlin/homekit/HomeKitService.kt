package homekit

import mdns.MulticastService
import mdns.packet.MulticastDnsPacket
import mdns.packet.MulticastDnsPacketHeader
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

    private val recordName = "$name.$protocol"
    private val targetName = "$name.local"
    private val tcpAnswer = PTRRecord(protocol, recordName, false, 4500)
    private val srvRecord = SRVRecord(recordName, targetName, settings.port, timeToLive = 4500, isCached = false)
    private val addressRecord = ARecord("$name.local", localhost.hostAddress, false, 4500)
    private val txtRecord = TXTRecord(recordName, false, 4500) {
        put("c#", settings.configurationNumber)
        put("id", settings.serverMAC)
        put("md", name)
        put("pv", "1.1")
        put("s#", "1")
        put("sf", 0x00) // TODO update when paired / unpaired
        put("ci", 2)
    }

    override fun condition(packet: MulticastDnsPacket): Boolean = !packet.header.isResponse
            && packet.queryRecords.any { it.label == protocol || it.label == recordName }
            && packet.answerRecords.none { it.label == protocol || it.label == recordName || it.label == targetName }

    override fun respond(socket: MulticastSocket, datagramPacket: DatagramPacket, packet: MulticastDnsPacket) {
        val desiredQueries = packet.queryRecords.filter { it.label == protocol || it.label == recordName }
        // Logger.info("[${datagramPacket.socketAddress}] is asking for ${desiredQueries.map { "[${if (it.hasProperty) "Unicast" else "Multicast"}] ${it.label}" }} with answers ${packet.answerRecords.map { "${it.type} ${it.label}" }}")
        val includePointer = desiredQueries.any { it.type == RecordType.PTR }
        val includeTXT = desiredQueries.any { it.type == RecordType.TXT }
        val includeSRV = desiredQueries.any { it.type == RecordType.SRV }
        val isUnicast = desiredQueries.any { it.hasProperty }
        val respondingPacket = MulticastDnsPacket(MulticastDnsPacketHeader(isResponse = true)).apply {
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

    override val wakeUpPacket: MulticastDnsPacket = MulticastDnsPacket(MulticastDnsPacketHeader(isResponse = true)).apply {
        answerRecords.add(tcpAnswer)
        additionalRecords.apply {
            add(srvRecord)
            add(addressRecord)
            add(txtRecord)
        }
    }

    fun updateTextRecords(isPaired: Boolean) {
        txtRecord.dataMap["sf"] = if (isPaired) 0 else 1
    }


}