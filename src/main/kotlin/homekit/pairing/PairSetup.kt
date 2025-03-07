package homekit.pairing

import homekit.Settings
import homekit.communication.HttpResponse
import homekit.communication.Session
import homekit.encryption.ChaCha
import homekit.encryption.Ed25519
import homekit.encryption.HKDF
import homekit.structure.data.Pairing
import homekit.structure.storage.PairingStorage
import homekit.tlv.TLVError
import homekit.tlv.TLVItem
import homekit.tlv.TLVPacket
import homekit.tlv.Tag
import utils.Constants
import utils.Logger
import utils.asBigInteger
import utils.asByteArray
import java.math.BigInteger
import java.nio.ByteBuffer
import java.util.*


/**
 * Created by Mihael Valentin Berčič
 * on 28/12/2020 at 13:38
 * using IntelliJ IDEA
 */
object PairSetup {

    private const val contentType = "application/pairing+tlv8"

    /**
     * Handles requests for pair-setup stage.
     *
     * @param settings bridge [Settings].
     * @param pairings currently established pairings.
     * @param session current [Session]
     * @param data data sent by the current controller.
     * @return a response containing information about current pair-setup stage.
     */
    fun handleRequest(settings: Settings, pairings: PairingStorage, session: Session, data: ByteArray): HttpResponse {
        val packet = TLVPacket(data)
        val stateItem = packet[Tag.State]
        val requestedValue = stateItem.dataList[0].toInt()

        if (requestedValue != session.currentState) throw Exception("Incorrect pair setup state.")

        return when (requestedValue) {
            1 -> computeStartingInformation(session)
            3 -> verifyDeviceProof(session, packet)
            5 -> decryptPublicInformation(settings, pairings, session, packet[Tag.EncryptedData])
            else -> HttpResponse(204, contentType)
        }

    }

    /**
     * Computes pairing password (PIN), that the user enters upon pairing start.
     *
     * @param session current session established with the controller.
     * @return HttpResponse containing encoded TLV packet.
     */
    private fun computeStartingInformation(session: Session): HttpResponse {
        val password = generatePin().apply { Logger.info("Pin: $this") }
        val srp = session.srp
        val publicKey = srp.computePublicKey(password)

        val responsePacket = TLVPacket(
            TLVItem(Tag.State, 2),
            TLVItem(Tag.Salt, *srp.salt),
            TLVItem(Tag.PublicKey, *publicKey.asByteArray)
        )
        session.currentState = 3
        return HttpResponse(contentType = contentType, data = responsePacket.asByteArray)
    }

    /**
     * Verifies the users' entered PIN.
     *
     * If the proofs do not match, a TLV error packet is returned.
     *
     * @param session currently established session.
     * @param packet [TLVPacket] sent by the controller through the current session.
     * @return an encoded [TLVPacket].
     */
    private fun verifyDeviceProof(session: Session, packet: TLVPacket): HttpResponse {
        val clientPublicKeyItem = packet[Tag.PublicKey]
        val clientEvidenceItem = packet[Tag.Proof]
        val srp = session.srp

        val clientKey = clientPublicKeyItem.dataArray.asBigInteger
        val clientEvidence = clientEvidenceItem.dataArray.asBigInteger
        val evidence = srp.verifyDevice(clientKey, clientEvidence)
        if (evidence == BigInteger.ZERO) return TLVErrorResponse(4, TLVError.Authentication)

        val responsePacket = TLVPacket(
            TLVItem(Tag.State, 4),
            TLVItem(Tag.Proof, *evidence.asByteArray)
        )
        session.currentState = 5
        Logger.trace("Responding with response packet!")
        return HttpResponse(contentType = contentType, data = responsePacket.asByteArray)
    }

    /**
     * Decrypts controllers public information and computes public / private key pair.
     *
     * Key pairs are stored in /bridge folder.
     *
     * @param settings [Settings] bridge settings.
     * @param pairings [PairingStorage] currently paired controllers.
     * @param session [Session] currently established session.
     * @param encryptedItem [TLVItem] encrypted TLV item sent from the controller.
     * @return
     */
    private fun decryptPublicInformation(settings: Settings, pairings: PairingStorage, session: Session, encryptedItem: TLVItem): HttpResponse {
        val encryptedData = encryptedItem.dataArray
        val srp = session.srp
        val sharedSecret = srp.sharedSecret
        val encryptionHKDF = HKDF.compute("HMACSHA512", sharedSecret, Constants.encryptionSalt, Constants.encryptionInfo, 32)

        val cipherBuffer = ByteBuffer.allocate(12 + encryptedData.size).apply {
            position(4)
            put("PS-Msg05".toByteArray())
            put(encryptedData)
        }

        val decryptedData = ChaCha.decrypt(cipherBuffer.array(), encryptionHKDF)

        val parsedPacket = TLVPacket(decryptedData)
        val devicePublicKey = parsedPacket[Tag.PublicKey].dataArray
        val deviceSignature = parsedPacket[Tag.Signature].dataArray
        val deviceIdentifier = parsedPacket[Tag.Identifier].dataArray

        val controllerHKDF = HKDF.compute("HMACSHA512", sharedSecret, Constants.controllerSalt, Constants.controllerInfo, 32)

        val deviceInfo = controllerHKDF + deviceIdentifier + devicePublicKey

        val deviceEdPublicKey = Ed25519.parsePublicKey(devicePublicKey)
        val isVerified = Ed25519.verifySignature(deviceEdPublicKey, deviceInfo, deviceSignature)
        if (!isVerified) throw Exception("Signature not verified...")

        val accessoryKeyPair = Ed25519.generateKeyPair().apply {
            Ed25519.storePrivateKey("bridge/ed25519-private", privateKey)
            Ed25519.storePublicKey("bridge/ed25519-public", publicKey)
        }

        val encodedPublicKey = Ed25519.encode(accessoryKeyPair.publicKey)

        val accessoryHKDF = HKDF.compute("HMACSHA512", sharedSecret, Constants.accessorySignSalt, Constants.accessorySignInfo, 32)
        val accessoryIdentifier = settings.serverMAC.toByteArray()
        val accessoryInfo = accessoryHKDF + accessoryIdentifier + encodedPublicKey

        val subPacket = TLVPacket(
            TLVItem(Tag.Identifier, *accessoryIdentifier),
            TLVItem(Tag.PublicKey, *encodedPublicKey),
            TLVItem(Tag.Signature, *Ed25519.sign(accessoryKeyPair.privateKey, accessoryInfo))
        )
        val subPacketArray = subPacket.asByteArray
        val encryptionBuffer = ByteBuffer.allocate(12 + subPacketArray.size).apply {
            position(4)
            put("PS-Msg06".toByteArray())
            put(subPacketArray)
        }
        val encodedSubPacket = ChaCha.encrypt(encryptionBuffer.array(), encryptionHKDF)

        val responsePacket = TLVPacket(
            TLVItem(Tag.State, 6),
            TLVItem(Tag.EncryptedData, *encodedSubPacket)
        )

        val controllerIdentifier = String(deviceIdentifier)
        pairings.addPairing(Pairing(controllerIdentifier, Ed25519.encode(deviceEdPublicKey), true))
        return HttpResponse(contentType = contentType, data = responsePacket.asByteArray)
    }

    /**
     * Generates a random PIN used for pair-setup procedure.
     *
     * @return A string of a randomly generated PIN of a format: "XXX-XX-XXX"
     */
    private fun generatePin(): String {
        val random = Random()
        val first = (0..2).map { random.nextInt(9) }.joinToString("")
        val second = (0..1).map { random.nextInt(9) }.joinToString("")
        val third = (0..2).map { random.nextInt(9) }.joinToString("")
        return "$first-$second-$third"
    }

}