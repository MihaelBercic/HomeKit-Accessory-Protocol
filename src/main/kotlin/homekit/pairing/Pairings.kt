package homekit.pairing

import homekit.encryption.Ed25519
import homekit.HomeKitService
import homekit.communication.HttpResponse
import homekit.communication.Session
import homekit.structure.data.Pairing
import homekit.structure.storage.PairingStorage
import homekit.tlv.*
import utils.Logger

/**
 * Created by Mihael Valentin Berčič
 * on 29/01/2021 at 09:39
 * using IntelliJ IDEA
 */
object Pairings {

    private const val contentType = "application/pairing+tlv8"

    /**
     * Decodes the TLV packet from the given byte array and decides which method was requested.
     *
     * @param session
     * @param homeKitService
     * @param pairings
     * @param data
     * @return A response to be sent back to the requester.
     */
    fun handleRequest(session: Session, homeKitService: HomeKitService, pairings: PairingStorage, data: ByteArray): HttpResponse {
        if (!session.currentController.isAdmin) return TLVErrorResponse(2, TLVError.Authentication)
        val packet = TLVPacket(data)
        val stateItem = packet[Tag.State]
        val methodItem = packet[Tag.Method]
        val pairingMethod = PairingMethod.valueOf(methodItem.dataArray[0])
        Logger.info("State: ${stateItem.dataArray[0]} looking for $pairingMethod")

        return when (pairingMethod) {
            PairingMethod.AddPairing -> addPairing(pairings, homeKitService, session, packet)
            PairingMethod.RemovePairing -> removePairing(pairings, session, homeKitService, packet)
            PairingMethod.ListPairings -> listPairings(pairings)
            else -> HttpResponse(404, "application/pairing+tlv8")
        }
    }

    /**
     * Adds the new pairing to the [PairingStorage].
     *
     * If the pairing request is not coming from an administrator, the request is denied and an error is returned.
     *
     * @param storage Current pairing storage of the bridge.
     * @param homeKitService
     * @param packet Current request packet.
     * @return TLV Encoded packet representing whether the addition was successful or not.
     */
    private fun addPairing(storage: PairingStorage, homeKitService: HomeKitService, session: Session, packet: TLVPacket): HttpResponse {
        val currentController = session.currentController
        Logger.trace("Add Pairing was called!")
        if (!currentController.isAdmin) return TLVErrorResponse(2, TLVError.Authentication)

        val additionalIdentifier = String(packet[Tag.Identifier].dataArray)
        val additionalPublicKey = Ed25519.parsePublicKey(packet[Tag.PublicKey].dataArray)
        val additionalPermissions = packet[Tag.Permissions].dataArray[0]
        val encodedAdditionalPublicKey = Ed25519.encode(additionalPublicKey)

        val existingPairing = storage.findPairing(additionalIdentifier)
        val shouldBeAdmin = additionalPermissions.compareTo(1) == 0

        Logger.trace("Existing pairing: ${existingPairing?.identifier}\n\t$shouldBeAdmin\n\t$additionalPermissions")
        if (existingPairing != null) {
            if (!encodedAdditionalPublicKey.contentEquals(existingPairing.publicKey)) return TLVErrorResponse(2, TLVError.Unknown).apply { Logger.error("Returned Unknown error!") }
            existingPairing.isAdmin = shouldBeAdmin
        } else storage.addPairing(Pairing(additionalIdentifier, encodedAdditionalPublicKey, shouldBeAdmin))
        if (storage.isPaired) homeKitService.updateTextRecords(true)
        Logger.trace("Returning a TLV response with state 2!")
        return HttpResponse(200, contentType, data = TLVPacket(TLVItem(Tag.State, 2)).asByteArray)
    }

    /**
     * Removes a specific pairing from the pairing storage.
     *
     * If the requested pairing removal is not coming from an admin, the request is denied and returned an error.
     *
     * @param storage
     * @param session Current session the request is coming from.
     * @param homeKitService In order to update our mDNS service advertisement.
     * @param packet Current TLV request packet.
     * @return A TLV response that represents whether the removal was successful or not.
     */
    private fun removePairing(storage: PairingStorage, session: Session, homeKitService: HomeKitService, packet: TLVPacket): HttpResponse {
        val method = packet[Tag.Method]
        val identifier = String(packet[Tag.Identifier].dataArray)
        val currentIdentifier = session.currentController
        val isAdmin = currentIdentifier.isAdmin
        Logger.info("On removing pairing, method used: $method for the identifier $identifier. Is it coming from admin? $isAdmin")
        if (!isAdmin) return TLVErrorResponse(2, TLVError.Authentication)
        storage.removePairing(identifier)
        if (!storage.isPaired) homeKitService.updateTextRecords(false)
        return  HttpResponse(200, contentType, data = TLVPacket(TLVItem(Tag.State, 2)).asByteArray)
    }

    /**
     * Returns a Response with all current pairings encoded in a TLV Packet.
     *
     * @param storage Current pairings are obtained from the pairing storage.
     * @return A TLV Packet with encoded pairings.
     */
    private fun listPairings(storage: PairingStorage): HttpResponse {
        val state = TLVItem(Tag.State, 2)
        val lastIndex = storage.list.lastIndex
        val items = storage.list.mapIndexed { index, pairing ->
            mutableListOf(
                TLVItem(Tag.Identifier, *pairing.identifier.toByteArray()),
                TLVItem(Tag.PublicKey, *pairing.publicKey),
                TLVItem(Tag.Permissions, if (pairing.isAdmin) 1 else 0)
            ).apply { if (index != lastIndex) add(TLVItem(Tag.Separator)) }
        }.flatten()
        return  HttpResponse(200, contentType, data = TLVPacket(state, *items.toTypedArray()).asByteArray)
    }


}