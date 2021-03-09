package homekit.pairing

import encryption.Ed25519
import homekit.HomeKitService
import homekit.communication.HttpResponse
import homekit.communication.Response
import homekit.communication.Session
import homekit.structure.data.Pairing
import homekit.structure.data.PairingStorage
import homekit.tlv.*
import utils.Logger

/**
 * Created by Mihael Valentin Berčič
 * on 29/01/2021 at 09:39
 * using IntelliJ IDEA
 */
object Pairings {

    fun handleRequest(session: Session, homeKitService: HomeKitService, pairings: PairingStorage, data: ByteArray): Response {
        if (!session.currentController.isAdmin) return TLVErrorResponse(2, TLVError.Authentication)
        val packet = TLVPacket(data)
        val stateItem = packet[Tag.State]
        val methodItem = packet[Tag.Method]
        val pairingMethod = PairingMethod.valueOf(methodItem.dataArray[0])
        Logger.info("State: ${stateItem.dataArray[0]} looking for $pairingMethod")

        return when (pairingMethod) {
            PairingMethod.AddPairing -> addPairing(pairings, homeKitService, packet)
            PairingMethod.RemovePairing -> removePairing(pairings, session, homeKitService, packet)
            PairingMethod.ListPairings -> listPairings(pairings)
            else -> HttpResponse(404, "application/pairing+tlv8")
        }
    }

    private fun addPairing(pairings: PairingStorage, homeKitService: HomeKitService, packet: TLVPacket): Response {
        val additionalIdentifier = String(packet[Tag.Identifier].dataArray)
        val additionalPublicKey = Ed25519.parsePublicKey(packet[Tag.PublicKey].dataArray)
        val additionalPermissions = packet[Tag.Permissions].dataArray[0]
        val encodedAdditionalPublicKey = Ed25519.encode(additionalPublicKey)
        val existingPairing = pairings.findPairing(additionalIdentifier)
        val isAdmin = additionalPermissions.compareTo(1) == 0
        if (existingPairing != null) {
            if (!encodedAdditionalPublicKey.contentEquals(existingPairing.publicKey)) return TLVErrorResponse(2, TLVError.Unknown)
            existingPairing.isAdmin = isAdmin
        } else pairings.addPairing(Pairing(additionalIdentifier, encodedAdditionalPublicKey, isAdmin))
        if (pairings.isPaired) homeKitService.updateTextRecords(true)
        return Response(TLVPacket(TLVItem(Tag.State, 2)).asByteArray)
    }

    private fun removePairing(pairingStorage: PairingStorage, session: Session, homeKitService: HomeKitService, packet: TLVPacket): Response {
        val method = packet[Tag.Method]
        val identifier = String(packet[Tag.Identifier].dataArray)
        val currentIdentifier = session.currentController
        val isAdmin = currentIdentifier.isAdmin
        Logger.info("On removing pairing, method used: $method for the identifier $identifier. Is it coming from admin? $isAdmin")
        if (!isAdmin) return TLVErrorResponse(2, TLVError.Authentication)
        pairingStorage.removePairing(identifier)
        session.shouldClose = true
        if (!pairingStorage.isPaired) homeKitService.updateTextRecords(false)
        return Response(TLVPacket(TLVItem(Tag.State, 2)).asByteArray)
    }

    private fun listPairings(pairingStorage: PairingStorage): Response {
        val state = TLVItem(Tag.State, 2)
        val items = pairingStorage.list.map { pairing ->
            listOf(
                TLVItem(Tag.Identifier, *pairing.identifier.toByteArray()),
                TLVItem(Tag.PublicKey, *pairing.publicKey),
                TLVItem(Tag.Permissions, if (pairing.isAdmin) 1 else 0)
            )
        }.flatten()
        return Response(TLVPacket(state, *items.toTypedArray()).asByteArray)
    }


}