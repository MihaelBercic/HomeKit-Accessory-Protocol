package homekit.pairing

import asBigInteger
import asByteArray
import homekit.pairing.srp.SRP
import homekit.tlv.*
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
        val stateItem = packet.get<StateItem>()

        val requestedValue = stateItem.value.toInt()
        println("Current state: $currentState vs. $requestedValue")
        if (requestedValue != currentState) return

        when (requestedValue) {
            1 -> computeStartingInformation(context)
            3 -> verifyDeviceProof(context, packet)
            5 -> {
                val encrypted = packet.get<EncryptedItem>()
                println("We have our encrypted item!")
                encrypted.thirdStep(srp)
            }
        }

    }

    private fun computeStartingInformation(context: Context) {
        val password = "111-11-111".apply { println("Pin: $this") }
        val publicKey = srp.performFirstStep(password)

        val state = StateItem(2)
        val saltItem = SaltItem(srp.salt)
        val pubicKeyItem = PublicKeyItem(publicKey.asByteArray)
        val responsePacket = Packet(state, pubicKeyItem, saltItem)
        currentState = 3
        context.result(responsePacket.toByteArray())
    }

    private fun verifyDeviceProof(context: Context, packet: Packet) {
        val clientPublicKeyItem = packet.get<PublicKeyItem>()
        val clientEvidenceItem = packet.get<EvidenceItem>()

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