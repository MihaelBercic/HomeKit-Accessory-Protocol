package homekit.encryption

import utils.asBigInteger
import utils.asByteArray
import java.io.File
import java.security.KeyFactory
import java.security.KeyPairGenerator
import java.security.Signature
import java.security.interfaces.EdECPrivateKey
import java.security.interfaces.EdECPublicKey
import java.security.spec.EdECPoint
import java.security.spec.EdECPrivateKeySpec
import java.security.spec.EdECPublicKeySpec
import java.security.spec.NamedParameterSpec

/**
 * Created by Mihael Valentin Berčič
 * on 20/01/2021 at 14:48
 * using IntelliJ IDEA
 */
object Ed25519 {

    fun generateKeyPair(): EdEcKeyPair {
        val keyPairGenerator = KeyPairGenerator.getInstance("Ed25519")
        val keyPair = keyPairGenerator.generateKeyPair()
        val privateKey = keyPair.private as EdECPrivateKey
        val publicKey = keyPair.public as EdECPublicKey
        return EdEcKeyPair(privateKey, publicKey)
    }

    fun storePrivateKey(path: String, privateKey: EdECPrivateKey) = File(path).writeBytes(encode(privateKey))

    fun storePublicKey(path: String, publicKey: EdECPublicKey) = File(path).writeBytes(encode(publicKey))

    fun verifySignature(publicKey: EdECPublicKey, data: ByteArray, signature: ByteArray): Boolean {
        val signatureInstance = Signature.getInstance("Ed25519").apply {
            initVerify(publicKey)
            update(data)
        }
        return signatureInstance.verify(signature)
    }

    fun sign(private: EdECPrivateKey, data: ByteArray): ByteArray {
        val signatureInstance = Signature.getInstance("Ed25519").apply {
            initSign(private)
            update(data)
        }
        return signatureInstance.sign()
    }

    fun encode(public: EdECPublicKey): ByteArray {
        val keyPoint = public.point
        val keyArray = keyPoint.y.asByteArray.reversedArray()
        val lastIndex = keyArray.lastIndex
        val lastByte = keyArray[lastIndex].toInt()
        if (keyPoint.isXOdd) keyArray[lastIndex] = (lastByte or 128).toByte()
        return keyArray
    }

    fun encode(privateKey: EdECPrivateKey): ByteArray = privateKey.bytes.orElseThrow()

    fun parsePrivateKey(byteArray: ByteArray): EdECPrivateKey {
        val keySpec = EdECPrivateKeySpec(NamedParameterSpec.ED25519, byteArray)
        return KeyFactory.getInstance("Ed25519").generatePrivate(keySpec) as EdECPrivateKey
    }

    fun loadPrivateKey(path: String) = parsePrivateKey(File(path).readBytes())

    fun loadPublicKey(path: String) = parsePublicKey(File(path).readBytes())

    fun parsePublicKey(byteArray: ByteArray): EdECPublicKey {
        val lastIndex = byteArray.lastIndex
        val lastByte = byteArray[lastIndex]
        val lastByteInt = lastByte.toInt()

        byteArray[lastIndex] = (lastByteInt and 127).toByte()

        val isDeviceXOdd = lastByteInt.and(255).shr(7) == 1
        val devicePublicY = byteArray.reversedArray().asBigInteger
        val point = EdECPoint(isDeviceXOdd, devicePublicY)

        val keyFactory = KeyFactory.getInstance("Ed25519")
        val nameSpec = NamedParameterSpec.ED25519
        val keySpec = EdECPublicKeySpec(nameSpec, point)
        return keyFactory.generatePublic(keySpec) as EdECPublicKey
    }

    data class EdEcKeyPair(val privateKey: EdECPrivateKey, val publicKey: EdECPublicKey)

}