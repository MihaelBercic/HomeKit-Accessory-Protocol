package homekit.pairing

import utils.asByteArray
import encryption.ChaCha
import encryption.Curve25519
import encryption.Ed25519
import homekit.Settings
import homekit.communication.HttpResponse
import homekit.communication.Response
import homekit.communication.Session
import homekit.structure.data.PairingStorage
import homekit.tlv.TLVError
import homekit.tlv.TLVItem
import homekit.tlv.TLVPacket
import homekit.tlv.Tag
import java.nio.ByteBuffer


/**
 * Created by Mihael Valentin Berčič
 * on 20/01/2021 at 13:43
 * using IntelliJ IDEA
 */

object PairVerify {

    private const val contentType = "application/pairing+tlv8"

    fun handleRequest(settings: Settings, pairings: PairingStorage, session: Session, data: ByteArray): Response {
        val tlvPacket = TLVPacket(data)
        val stateItem = tlvPacket[Tag.State]
        val requestedState = stateItem.dataList[0].toInt()

        if (session.currentState != requestedState) throw Exception("State mismatch.")
        return when (requestedState) {
            1 -> generateCurveKey(settings, session, tlvPacket)
            3 -> verifyDeviceInformation(pairings, session, tlvPacket[Tag.EncryptedData])
            else -> HttpResponse(204, contentType)
        }
    }


    private fun generateCurveKey(settings: Settings, session: Session, packet: TLVPacket): HttpResponse {
        val edPrivate = Ed25519.loadPrivateKey("bridge/ed25519-private")
        val deviceCurveKeyArray = packet[Tag.PublicKey].dataArray.reversedArray()

        val deviceCurveKey = Curve25519.decode(deviceCurveKeyArray)
        val curveKeyPair = Curve25519.generateKeyPair()

        Curve25519.computeSharedSecret(curveKeyPair.privateKey, deviceCurveKey).apply {
            session.setSharedSecret(this)
        }
        val serverMACArray = settings.serverMAC.toByteArray()
        val encodedPublicKey = curveKeyPair.publicKey.u.asByteArray.reversedArray()
        val accessoryInfo = encodedPublicKey + serverMACArray + deviceCurveKeyArray.reversedArray()
        val infoSignature = Ed25519.sign(edPrivate, accessoryInfo)

        val subTlv = TLVPacket(
            TLVItem(Tag.Identifier, *serverMACArray),
            TLVItem(Tag.Signature, *infoSignature)
        )
        val subArray = subTlv.asByteArray
        val subBuffer = ByteBuffer.allocate(12 + subArray.size).apply {
            position(4)
            put("PV-Msg02".toByteArray())
            put(subArray)
        }
        val encrypted = ChaCha.encrypt(subBuffer.array(), session.sessionKey)

        val responsePacket = TLVPacket(
            TLVItem(Tag.State, 2),
            TLVItem(Tag.EncryptedData, *encrypted),
            TLVItem(Tag.PublicKey, *encodedPublicKey)
        )

        session.currentState = 3
        return HttpResponse(contentType = contentType, data = responsePacket.asByteArray)
    }

    private fun verifyDeviceInformation(pairings: PairingStorage, session: Session, encryptedItem: TLVItem): Response {
        val encryptedData = encryptedItem.dataArray
        val dataBuffer = ByteBuffer.allocate(12 + encryptedData.size).apply {
            position(4)
            put("PV-Msg03".toByteArray())
            put(encryptedData)
        }
        val decoded = ChaCha.decrypt(dataBuffer.array(), session.sessionKey)
        val parsedPacket = TLVPacket(decoded)
        val controllerIdentifier = String(parsedPacket[Tag.Identifier].dataArray)

        val pairing = pairings.findPairing(controllerIdentifier) ?: return TLVErrorResponse(4, TLVError.Authentication)
        session.apply {
            currentController = pairing
            currentState = 1
            isSecure = true
        }
        return HttpResponse(contentType = contentType, data = TLVPacket(TLVItem(Tag.State, 0x04)).asByteArray)
    }

}
