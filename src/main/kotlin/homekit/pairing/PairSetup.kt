package homekit.pairing

import asHexString
import com.nimbusds.srp6.BigIntegerUtils
import com.nimbusds.srp6.SRP6CryptoParams
import com.nimbusds.srp6.SRP6Routines
import homekit.tlv.ProofItem
import homekit.tlv.PublicKeyItem
import homekit.tlv.SaltItem
import homekit.tlv.StateItem
import homekit.tlv.structure.Packet
import io.javalin.http.Context
import java.math.BigInteger
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.*

/**
 * Created by Mihael Valentin Berčič
 * on 28/12/2020 at 13:38
 * using IntelliJ IDEA
 */
class PairSetup {

    var currentState: Int = 1
    private val secureRandom = SecureRandom()
    private val N = SRP6CryptoParams.N_3072
    private val g = SRP6CryptoParams.g_large
    private val digest = MessageDigest.getInstance("SHA-512")
    private val routines = SRP6Routines()

    private val username = "Pair-Setup"
    private lateinit var v: BigInteger
    private lateinit var k: BigInteger
    private lateinit var b: BigInteger
    private lateinit var B: BigInteger
    private lateinit var A: BigInteger


    fun handleRequest(context: Context) {
        context.header("Content-Type", "application/pairing+tlv8")
        val packet = Packet(context.bodyAsBytes())
        val stateItem = packet.find { it is StateItem } as? StateItem ?: return

        val requestedValue = stateItem.value.toInt()
        println("Current state: $currentState vs. $requestedValue")
        if (requestedValue != currentState) return

        when (requestedValue) {
            1 -> firstStep(context)
            3 -> secondStep(context, packet)
        }

    }

    private fun firstStep(context: Context) {
        val password = "111-11-111".apply { println("Pin: $this") }
        val salt = routines.generateRandomSalt(16)
        val x = routines.computeX(digest, salt, password.toByteArray()).apply { digest.reset() }

        v = routines.computeVerifier(N, g, x)
        k = routines.computeK(digest, N, g).apply { digest.reset() }
        b = routines.generatePrivateValue(N, secureRandom)
        B = routines.computePublicServerValue(N, g, k, v, b)

        val state = StateItem(2)
        val saltItem = SaltItem(salt)
        val pubicKeyItem = PublicKeyItem(BigIntegerUtils.bigIntegerToBytes(B))
        val responsePacket = Packet(state, pubicKeyItem, saltItem)
        currentState = 3
        context.result(responsePacket.toByteArray())
    }

    private fun secondStep(context: Context, packet: Packet) {
        val clientPublicKey = packet.find { it is PublicKeyItem } as? PublicKeyItem ?: return
        val clientProofItem = packet.find { it is ProofItem } as? ProofItem ?: return

        A = BigIntegerUtils.bigIntegerFromBytes(clientPublicKey.data.toByteArray())

        val u = routines.computeU(digest, N, A, B).apply { digest.reset() }
        val S = routines.computeSessionKey(N, v, u, A, b)
        val computedClientEvidence = routines.computeClientEvidence(digest, A, B, S).apply { digest.reset() }
        val clientEvidence = BigIntegerUtils.bigIntegerFromBytes(clientProofItem.data.toByteArray())

        println("Client:")
        println(clientEvidence.toByteArray().asHexString)

        println("\nComputed:")
        println(computedClientEvidence.toByteArray().asHexString)
        println(clientEvidence == computedClientEvidence)
        currentState = 6
    }

    private fun generatePin(): String {
        val random = Random()
        val first = (0..2).map { random.nextInt(9) }.joinToString("")
        val second = (0..1).map { random.nextInt(9) }.joinToString("")
        val third = (0..2).map { random.nextInt(9) }.joinToString("")
        return "$first-$second-$third"
    }

}