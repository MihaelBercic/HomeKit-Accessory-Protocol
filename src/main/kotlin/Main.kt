import mdns.Header
import mdns.Packet
import mdns.asDatagramPacket
import mdns.records.PTRRecord
import mdns.records.QueryRecord
import mdns.records.SRVRecord
import mdns.records.TXTRecord
import java.net.InetAddress
import java.net.MulticastSocket

/**
 * Created by Mihael Valentin Berčič
 * on 08/12/2020 at 22:41
 * using IntelliJ IDEA
 */

const val IPv4 = "224.0.0.251"
const val IPv6 = "ff02::fb"
const val mDNS = 5353
val localhost = InetAddress.getLocalHost()
val destination = InetAddress.getByName(IPv4)


fun main() {

    val header = Header(identification = 0, isResponse = false)

    val packet = Packet(header).apply {
        val question = QueryRecord("_hap._tcp.local")
        val domain = PTRRecord("_sex._tcp.local", "fyuck")
        val info = TXTRecord("_hap._tcp.local", mutableMapOf("Fuck" to 5))
        val srv = SRVRecord("_hap._tcp.local", 0, 10, 3000, "HAP-Bridge")
        queryRecords.add(question)
        answerRecords.add(domain)
        additionalRecords.add(info)
        additionalRecords.add(srv)
    }

    MulticastSocket(mDNS).apply {
        joinGroup(destination)
        send(packet.asDatagramPacket)
    }
}

/*
 00 00 29 05 a0 00 00 11 94 00 12 00 04
00 0e 00 45 6a 4b aa 9b f9 9e 7e 61 c5 2f 6b 85
 */