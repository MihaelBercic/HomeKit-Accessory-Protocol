package homekit.pairing

import utils.Logger
import encryption.Ed25519
import homekit.communication.HttpResponse
import homekit.communication.Response
import homekit.communication.Session
import homekit.communication.structure.data.Pairing
import homekit.communication.structure.data.PairingStorage
import homekit.tlv.*

/**
 * Created by Mihael Valentin Berčič
 * on 29/01/2021 at 09:39
 * using IntelliJ IDEA
 */
object Pairings {

    fun handleRequest(session: Session, pairings: PairingStorage, data: ByteArray): Response {
        if (!session.currentController.isAdmin) return TLVErrorResponse(2, TLVError.Authentication)
        val packet = TLVPacket(data)
        val stateItem = packet[TLVValue.State]
        val methodItem = packet[TLVValue.Method]
        val pairingMethod = PairingMethod.valueOf(methodItem.dataArray[0])
        Logger.info("State: ${stateItem.dataArray[0]} looking for $pairingMethod")

        return when (pairingMethod) {
            PairingMethod.AddPairing -> addPairing(pairings, packet)
            PairingMethod.RemovePairing -> removePairing(pairings, session, packet)
            PairingMethod.ListPairings -> listPairings(pairings)
            else -> HttpResponse(404, "application/pairing+tlv8")
        }
    }

    private fun addPairing(pairings: PairingStorage, packet: TLVPacket): Response {
        val additionalIdentifier = String(packet[TLVValue.Identifier].dataArray)
        val additionalPublicKey = Ed25519.parsePublicKey(packet[TLVValue.PublicKey].dataArray)
        val additionalPermissions = packet[TLVValue.Permissions].dataArray[0]
        val encodedAdditionalPublicKey = Ed25519.encode(additionalPublicKey)
        val existingPairing = pairings.findPairing(additionalIdentifier)
        val isAdmin = additionalPermissions.compareTo(1) == 0
        if (existingPairing != null) {
            if (!encodedAdditionalPublicKey.contentEquals(existingPairing.publicKey)) return TLVErrorResponse(2, TLVError.Unknown)
            existingPairing.isAdmin = isAdmin
        } else pairings.addPairing(Pairing(additionalIdentifier, encodedAdditionalPublicKey, isAdmin))
        return Response(TLVPacket(TLVItem(TLVValue.State, 2)).asByteArray)
    }

    private fun removePairing(pairingStorage: PairingStorage, session: Session, packet: TLVPacket): Response {
        val method = packet[TLVValue.Method]
        val identifier = String(packet[TLVValue.Identifier].dataArray)
        val currentIdentifier = session.currentController
        val isAdmin = currentIdentifier.isAdmin
        Logger.info("On removing pairing, method used: $method for the identifier $identifier. Is it coming from admin? $isAdmin")
        if (!isAdmin) return TLVErrorResponse(2, TLVError.Authentication)
        pairingStorage.removePairing(identifier)
        session.shouldClose = true
        return Response(TLVPacket(TLVItem(TLVValue.State, 2)).asByteArray)
    }

    private fun listPairings(pairingStorage: PairingStorage): Response {
        val state = TLVItem(TLVValue.State, 2)
        val items = pairingStorage.list.map { pairing ->
            listOf(
                TLVItem(TLVValue.Identifier, *pairing.identifier.toByteArray()),
                TLVItem(TLVValue.PublicKey, *pairing.publicKey),
                TLVItem(TLVValue.Permissions, if (pairing.isAdmin) 1 else 0)
            )
        }.flatten()
        return Response(TLVPacket(state, *items.toTypedArray()).asByteArray)
    }


}