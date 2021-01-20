package homekit.pairing

import asBigInteger
import asByteArray
import homekit.pairing.encryption.ChaCha
import homekit.pairing.encryption.HKDF
import homekit.pairing.srp.SRP
import homekit.serverMAC
import homekit.tlv.structure.TLVItem
import homekit.tlv.structure.TLVPacket
import homekit.tlv.structure.TLVValue
import io.javalin.http.Context
import java.nio.ByteBuffer
import java.security.KeyFactory
import java.security.KeyPairGenerator
import java.security.Signature
import java.security.interfaces.EdECPublicKey
import java.security.spec.EdECPoint
import java.security.spec.EdECPublicKeySpec
import java.security.spec.NamedParameterSpec
import java.util.*


/**
 * Created by Mihael Valentin Berčič
 * on 28/12/2020 at 13:38
 * using IntelliJ IDEA
 */
class PairSetup {

    private val srp = SRP()

    private var currentState: Int = 1

    private val encryptionSalt = "Pair-Setup-Encrypt-Salt".toByteArray()
    private val encryptionInfo = "Pair-Setup-Encrypt-Info".toByteArray()

    private val controllerSalt = "Pair-Setup-Controller-Sign-Salt".toByteArray()
    private val controllerInfo = "Pair-Setup-Controller-Sign-Info".toByteArray()

    private val accessorySignSalt = "Pair-Setup-Accessory-Sign-Salt".toByteArray()
    private val accessorySignInfo = "Pair-Setup-Accessory-Sign-Info".toByteArray()

    fun handleRequest(context: Context) {
        context.header("Content-Type", "application/pairing+tlv8")
        val packet = TLVPacket(context.bodyAsBytes())

        val stateItem = packet[TLVValue.State]
        val requestedValue = stateItem.data[0].toInt()

        println("Current state: $currentState vs. $requestedValue")
        if (requestedValue != currentState) return

        when (requestedValue) {
            1 -> computeStartingInformation(context)
            3 -> verifyDeviceProof(context, packet)
            5 -> decryptPublicInformation(context, packet)
        }

    }

    private fun computeStartingInformation(context: Context) {
        val password = "111-11-111".apply { println("Pin: $this") }
        val publicKey = srp.performFirstStep(password)

        val responsePacket = TLVPacket(
            TLVItem(TLVValue.State, 2),
            TLVItem(TLVValue.Salt, *srp.salt),
            TLVItem(TLVValue.PublicKey, *publicKey.asByteArray)
        )
        currentState = 3
        context.result(responsePacket.toByteArray())
    }

    private fun verifyDeviceProof(context: Context, packet: TLVPacket) {
        val clientPublicKeyItem = packet[TLVValue.PublicKey]
        val clientEvidenceItem = packet[TLVValue.Proof]

        val clientKey = clientPublicKeyItem.data.toByteArray().asBigInteger
        val clientEvidence = clientEvidenceItem.data.toByteArray().asBigInteger
        val evidence = srp.performSecondStep(clientKey, clientEvidence)

        val responsePacket = TLVPacket(
            TLVItem(TLVValue.State, 4),
            TLVItem(TLVValue.Proof, *evidence.asByteArray)
        )
        currentState = 5
        context.result(responsePacket.toByteArray())
    }

    private fun decryptPublicInformation(context: Context, packet: TLVPacket) {
        val deviceEncryptedItem = packet[TLVValue.EncryptedData]
        val encryptedData = deviceEncryptedItem.data.toByteArray()
        val sharedSecret = srp.sharedSecret
        val encryptionHKDF = HKDF.compute("HMACSHA512", sharedSecret, encryptionSalt, encryptionInfo, 32)

        val cipherBuffer = ByteBuffer.allocate(12 + encryptedData.size).apply {
            position(4)
            put("PS-Msg05".toByteArray())
            put(encryptedData)
        }

        val decryptedData = ChaCha.decrypt(cipherBuffer.array(), encryptionHKDF)

        val parsedPacket = TLVPacket(decryptedData)
        val identifierItem = parsedPacket[TLVValue.Identifier]
        val publicKeyItem = parsedPacket[TLVValue.PublicKey]
        val signatureItem = parsedPacket[TLVValue.Signature]

        val devicePublicKey = publicKeyItem.data.toByteArray()
        val deviceSignature = signatureItem.data.toByteArray()
        val deviceIdentifier = identifierItem.data.toByteArray()

        val controllerHKDF = HKDF.compute("HMACSHA512", sharedSecret, controllerSalt, controllerInfo, 32)

        val deviceInfo = controllerHKDF + deviceIdentifier + devicePublicKey

        val deviceLastIndex = devicePublicKey.lastIndex
        val deviceLastByte = devicePublicKey[deviceLastIndex]
        val deviceLastBytesInt = deviceLastByte.toInt()

        devicePublicKey[deviceLastIndex] = (deviceLastBytesInt and 127).toByte()

        val isDeviceXOdd = deviceLastBytesInt.and(255).shr(7) == 1
        val devicePublicY = devicePublicKey.reversedArray().asBigInteger

        val keyFactory = KeyFactory.getInstance("Ed25519")
        val nameSpec = NamedParameterSpec.ED25519
        val point = EdECPoint(isDeviceXOdd, devicePublicY)
        val keySpec = EdECPublicKeySpec(nameSpec, point)
        val key = keyFactory.generatePublic(keySpec)

        val signatureInstance = Signature.getInstance("Ed25519")
        signatureInstance.apply {
            initVerify(key)
            update(deviceInfo)
            if (!verify(deviceSignature)) return // TODO respond with TLV Error
        }

        val keyPairGenerator = KeyPairGenerator.getInstance("Ed25519")
        val keyPair = keyPairGenerator.generateKeyPair()
        val privateKey = keyPair.private
        val publicKey = keyPair.public as EdECPublicKey

        val publicKeyPoint = publicKey.point
        val isPublicKeyXOdd = publicKeyPoint.isXOdd
        val publicKeyY = publicKeyPoint.y
        val publicKeyArray = publicKeyY.asByteArray.reversedArray() // as little endian

        val lastIndex = publicKeyArray.lastIndex
        val lastByte = publicKeyArray[lastIndex].toInt()
        if (isPublicKeyXOdd) publicKeyArray[lastIndex] = (lastByte or 128).toByte()

        val accessoryHKDF = HKDF.compute("HMACSHA512", sharedSecret, accessorySignSalt, accessorySignInfo, 32)

        val accessoryIdentifier = serverMAC.toByteArray()

        val accessoryInfo = accessoryHKDF + accessoryIdentifier + publicKeyArray

        signatureInstance.apply {
            initSign(privateKey)
            update(accessoryInfo)
        }

        val subPacket = TLVPacket(
            TLVItem(TLVValue.Identifier, *accessoryIdentifier),
            TLVItem(TLVValue.PublicKey, *publicKeyArray),
            TLVItem(TLVValue.Signature, *signatureInstance.sign())
        )
        val encodedPacket = subPacket.toByteArray()
        val encodedSubPacket = ChaCha.encrypt(encodedPacket, encryptionHKDF, "PS-Msg06")

        val responsePacket = TLVPacket(
            TLVItem(TLVValue.State, 6),
            TLVItem(TLVValue.EncryptedData, *encodedSubPacket)
        )

        println("Responding to the request!")
        context.result(responsePacket.toByteArray())
    }

    private fun generatePin(): String {
        val random = Random()
        val first = (0..2).map { random.nextInt(9) }.joinToString("")
        val second = (0..1).map { random.nextInt(9) }.joinToString("")
        val third = (0..2).map { random.nextInt(9) }.joinToString("")
        return "$first-$second-$third"
    }

}