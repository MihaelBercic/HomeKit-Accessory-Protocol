package homekit.pairing

import homekit.communication.HttpResponse
import homekit.communication.Response
import homekit.communication.Session
import homekit.communication.structure.data.Pairing
import homekit.pairing.encryption.Ed25519
import homekit.tlv.structure.*

/**
 * Created by Mihael Valentin Berčič
 * on 29/01/2021 at 09:39
 * using IntelliJ IDEA
 */
object Pairings {

    fun handleRequest(session: Session, pairings: MutableList<Pairing>, data: ByteArray): Response {
        if (!session.currentController.isAdmin) return TLVErrorResponse(2, TLVError.Authentication)
        val packet = TLVPacket(data)
        val stateItem = packet[TLVValue.State]
        val methodItem = packet[TLVValue.Method]
        val pairingMethod = PairingMethod.valueOf(methodItem.dataArray[0])
        Logger.info("State: ${stateItem.dataArray[0]} looking for $pairingMethod")

        return when (pairingMethod) {
            PairingMethod.AddPairing -> addPairing(pairings, packet)
            PairingMethod.RemovePairing -> removePairing()
            PairingMethod.ListPairings -> listPairings(pairings)
            else -> HttpResponse(404, "application/pairing+tlv8")
        }
    }

    private fun addPairing(pairings: MutableList<Pairing>, packet: TLVPacket): Response {
        val additionalIdentifier = String(packet[TLVValue.Identifier].dataArray)
        val additionalPublicKey = Ed25519.parsePublicKey(packet[TLVValue.PublicKey].dataArray)
        val additionalPermissions = packet[TLVValue.Permissions].dataArray[0]
        val encodedAPK = Ed25519.encode(additionalPublicKey)
        val existingPairing = pairings.firstOrNull { it.identifier == additionalIdentifier }
        val isAdmin = additionalPermissions.compareTo(1) == 0
        if (existingPairing != null) {
            if (!encodedAPK.contentEquals(existingPairing.publicKey)) return TLVErrorResponse(2, TLVError.Unknown)
            existingPairing.isAdmin = isAdmin
        } else pairings.add(Pairing(additionalIdentifier, encodedAPK, isAdmin))
        return Response(TLVPacket(TLVItem(TLVValue.State, 2)).toByteArray())
    }

    private fun removePairing(): Response {
        // TODO
        return Response(byteArrayOf())
    }

    private fun listPairings(pairings: MutableList<Pairing>): Response {
        val state = TLVItem(TLVValue.State, 2)
        val items = pairings.map { pairing ->
            listOf(
                TLVItem(TLVValue.Identifier, *pairing.identifier.toByteArray()),
                TLVItem(TLVValue.PublicKey, *pairing.publicKey),
                TLVItem(TLVValue.Permissions, if (pairing.isAdmin) 1 else 0)
            )
        }.flatten()
        return Response(TLVPacket(state, *items.toTypedArray()).toByteArray())
    }


}

class TLVErrorResponse(state: Byte, error: TLVError) : Response(TLVPacket(TLVItem(TLVValue.State, state), TLVItem(TLVValue.Error, error.code)).toByteArray())