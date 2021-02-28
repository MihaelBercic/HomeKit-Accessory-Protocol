package homekit.pairing

import asByteArray
import homekit.Settings
import homekit.communication.HttpResponse
import homekit.communication.Response
import homekit.communication.Session
import homekit.communication.structure.data.PairingStorage
import homekit.pairing.encryption.ChaCha
import homekit.pairing.encryption.Curve25519
import homekit.pairing.encryption.Ed25519
import homekit.tlv.structure.TLVError
import homekit.tlv.structure.TLVItem
import homekit.tlv.structure.TLVPacket
import homekit.tlv.structure.TLVValue
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
        val stateItem = tlvPacket[TLVValue.State]
        val requestedState = stateItem.dataList[0].toInt()

        if (session.currentState != requestedState) throw Exception("State mismatch.")
        return when (requestedState) {
            1 -> generateCurveKey(settings, session, tlvPacket)
            3 -> verifyDeviceInformation(pairings, session, tlvPacket[TLVValue.EncryptedData])
            else -> HttpResponse(204, contentType)
        }
    }


    private fun generateCurveKey(settings: Settings, session: Session, packet: TLVPacket): HttpResponse {
        val edPrivate = Ed25519.loadPrivateKey("communication/ed25519-private")
        val deviceCurveKeyArray = packet[TLVValue.PublicKey].dataArray.reversedArray()

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
            TLVItem(TLVValue.Identifier, *serverMACArray),
            TLVItem(TLVValue.Signature, *infoSignature)
        )
        val subArray = subTlv.toByteArray()
        val subBuffer = ByteBuffer.allocate(12 + subArray.size).apply {
            position(4)
            put("PV-Msg02".toByteArray())
            put(subArray)
        }
        val encrypted = ChaCha.encrypt(subBuffer.array(), session.sessionKey)

        val responsePacket = TLVPacket(
            TLVItem(TLVValue.State, 2),
            TLVItem(TLVValue.EncryptedData, *encrypted),
            TLVItem(TLVValue.PublicKey, *encodedPublicKey)
        )

        session.currentState = 3
        return HttpResponse(contentType = contentType, data = *responsePacket.toByteArray())
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
        val controllerIdentifier = String(parsedPacket[TLVValue.Identifier].dataArray)

        val pairing = pairings.findPairing(controllerIdentifier) ?: return TLVErrorResponse(4, TLVError.Authentication)
        session.apply {
            currentState = 1
            isSecure = true
            currentController = pairing
        }
        return HttpResponse(contentType = contentType, data = *TLVPacket(TLVItem(TLVValue.State, 0x04)).toByteArray())
    }

}
