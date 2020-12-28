package homekit

import com.nimbusds.srp6.SRP6CryptoParams
import com.nimbusds.srp6.SRP6ServerSession
import com.nimbusds.srp6.SRP6VerifierGenerator
import homekit.pairing.PairSetup
import homekit.tlv.PublicKeyItem
import homekit.tlv.SaltItem
import homekit.tlv.StateItem
import homekit.tlv.structure.Packet
import io.javalin.Javalin
import java.math.BigInteger
import java.nio.ByteBuffer

/**
 * Created by Mihael Valentin Berčič
 * on 22/12/2020 at 23:38
 * using IntelliJ IDEA
 */
class HomeKitServer : Javalin() {

    private val pairSetup = PairSetup()

    init {

        post("/pair-setup") { context ->
            pairSetup.handleRequest(context)

            val buffer = ByteBuffer.wrap(context.bodyAsBytes())
            val packet = Packet(context.bodyAsBytes())
            val stateItem = packet.find { it is StateItem } as? StateItem ?: return@post
            when (stateItem.value.toInt().apply { println("Current state: $this") }) {
                1 -> {
                    val N = SRP6CryptoParams.N_3072
                    val g = SRP6CryptoParams.g_large
                    val username = "Pair-Setup"
                    val password = "111-22-333"

                    val crypto = SRP6CryptoParams(N, g, "SHA-512")
                    val generator = SRP6VerifierGenerator(crypto)
                    val server = SRP6ServerSession(crypto)

                    val salt = BigInteger(generator.generateRandomSalt(16))
                    val verifier = generator.generateVerifier(salt, username, password)
                    val publicKey = server.step1(username, salt, verifier)

                    val state = StateItem(2)
                    val publicKeyItemFirst = PublicKeyItem(publicKey.toByteArray())
                    val saltItem = SaltItem(salt)
                    val responsePacket = Packet(state, publicKeyItemFirst, saltItem)
                    context
                        .header("Content-Type", "application/pairing+tlv8")
                        .result(responsePacket.toByteArray())
                }
            }
        }

        post("/pair-verify") { println("Data from VERIFY!") }
        post("/accessories") { println("Data from ACCESSORIES!") }
        post("/pairings") { println("Data from PAIRINGS!") }

        start(3000)
    }

/*
/characteristics GET
/characteristics PUT
/identify POST
/pair-setup POST
/pair-verify POST
/pairings POST
/prepare PUT
/accessories GET
Retrieve the accessory attribute database from the accessory. Only valid from paired controllers. See ”6.6 IP Accessory Attribute Database” (page 60).

/resource
POST
Request the accessory to run the resource routine (e.g a snapshot is re- turned in response for an IP camera accessory). See ”11.5 Image Snapshot” (page 242).
 */
}


