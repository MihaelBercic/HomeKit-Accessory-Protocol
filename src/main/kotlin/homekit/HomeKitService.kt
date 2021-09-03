package homekit

import homekit.structure.storage.PairingStorage
import mdns.MulticastService
import mdns.packet.MulticastDnsPacket
import mdns.packet.MulticastDnsPacketHeader
import mdns.records.ARecord
import mdns.records.PTRRecord
import mdns.records.SRVRecord
import mdns.records.TXTRecord
import mdns.records.structure.IncompleteRecord
import mdns.records.structure.RecordType
import utils.Logger
import java.lang.Thread.sleep
import java.net.DatagramPacket
import java.net.InetAddress
import java.net.MulticastSocket
import kotlin.random.Random

/**
 * Created by Mihael Valentin Berčič
 * on 22/12/2020 at 13:44
 * using IntelliJ IDEA
 *
 * This class is a representation of a HomeKit _hap._tcp.local service that will be advertised on the local network.
 */
class HomeKitService(settings: Settings, pairingStorage: PairingStorage, name: String = "HomeServer", localhost: InetAddress) : MulticastService("_hap._tcp.local", localhost) {

    private val recordName = "$name.$protocol"
    private val targetName = "$name.local"

    private val tcpAnswer = PTRRecord(protocol, recordName, true, 4500)
    private val srvRecord = SRVRecord(recordName, targetName, settings.port, timeToLive = 4500, isCached = true)
    private val addressRecord = ARecord("$name.local", localhost.hostAddress, true, 4500)
    private val txtRecord = TXTRecord(recordName, true, 4500) {
        put("c#", settings.configurationNumber)
        put("id", settings.serverMAC)
        put("md", name)
        put("pv", "1.1")
        put("s#", "1")
        put("sf", if (pairingStorage.isPaired.apply { Logger.info("Is paired? ${pairingStorage.isPaired}") }) 0 else 1)
        put("ci", 2)
    }

    private val recordPredicate: (T: IncompleteRecord) -> Boolean = { it.label == protocol || it.label == recordName || it.label == targetName }

    override fun condition(packet: MulticastDnsPacket): Boolean {
        val queries = packet.queryRecords.filter(recordPredicate)
        val answers = packet.answerRecords.filter(recordPredicate)
        val isOutdated = answers.any { it.timeToLive <= 1250 }
        val allAnswered = queries.all { query -> answers.any { it.label == query.label } }
        // if (allAnswered && packet.header.isResponse && !isOutdated) return false
        return isOutdated || !allAnswered
    }

    override fun respond(socket: MulticastSocket, datagramPacket: DatagramPacket, packet: MulticastDnsPacket) {
        val desiredQueries = packet.queryRecords.filter { it.label == protocol || it.label == recordName }
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
        sleep(Random.nextLong(100, 300))
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