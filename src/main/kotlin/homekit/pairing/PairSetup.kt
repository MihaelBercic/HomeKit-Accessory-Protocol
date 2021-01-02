package homekit.pairing

import asBigInteger
import asByteArray
import homekit.pairing.srp.SRP
import homekit.tlv.EvidenceItem
import homekit.tlv.PublicKeyItem
import homekit.tlv.SaltItem
import homekit.tlv.StateItem
import homekit.tlv.structure.Packet
import io.javalin.http.Context
import java.util.*


/**
 * Created by Mihael Valentin Berčič
 * on 28/12/2020 at 13:38
 * using IntelliJ IDEA
 */
class PairSetup {

    var currentState: Int = 1

    private val srp = SRP()

    @Synchronized
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
        val password = generatePin().apply { println("Pin: $this") }
        val publicKey = srp.performFirstStep(password)

        val state = StateItem(2)
        val saltItem = SaltItem(srp.salt)
        val pubicKeyItem = PublicKeyItem(publicKey.asByteArray)
        val responsePacket = Packet(state, pubicKeyItem, saltItem)
        currentState = 3
        context.result(responsePacket.toByteArray())
    }

    private fun secondStep(context: Context, packet: Packet) {
        val clientPublicKeyItem = packet.find { it is PublicKeyItem } as? PublicKeyItem ?: return
        val clientEvidenceItem = packet.find { it is EvidenceItem } as? EvidenceItem ?: return

        val clientKey = clientPublicKeyItem.data.toByteArray().asBigInteger
        val clientEvidence = clientEvidenceItem.data.toByteArray().asBigInteger
        val evidence = srp.performSecondStep(clientKey, clientEvidence)

        val stateItem = StateItem(4)
        val proofItem = EvidenceItem(evidence.asByteArray)
        currentState = 5
        context.result(Packet(stateItem, proofItem).toByteArray())
    }

    private fun generatePin(): String {
        val random = Random()
        val first = (0..2).map { random.nextInt(9) }.joinToString("")
        val second = (0..1).map { random.nextInt(9) }.joinToString("")
        val third = (0..2).map { random.nextInt(9) }.joinToString("")
        return "$first-$second-$third"
    }

}