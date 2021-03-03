package encryption

import asBigInteger
import java.security.KeyFactory
import java.security.KeyPairGenerator
import java.security.interfaces.XECPrivateKey
import java.security.interfaces.XECPublicKey
import java.security.spec.NamedParameterSpec
import java.security.spec.XECPublicKeySpec
import javax.crypto.KeyAgreement

/**
 * Created by Mihael Valentin Berčič
 * on 20/01/2021 at 16:55
 * using IntelliJ IDEA
 */
object Curve25519 {

    fun generateKeyPair(): CurveKeyPair {
        val keyPairGenerator = KeyPairGenerator.getInstance("X25519");
        val keyPair = keyPairGenerator.generateKeyPair();
        val privateKey = keyPair.private as XECPrivateKey
        val publicKey = keyPair.public as XECPublicKey
        return CurveKeyPair(privateKey, publicKey)
    }

    fun decode(byteArray: ByteArray): XECPublicKey {
        val kf = KeyFactory.getInstance("X25519");
        val u = byteArray.asBigInteger
        val pubSpec = XECPublicKeySpec(NamedParameterSpec("X25519"), u);
        return kf.generatePublic(pubSpec) as XECPublicKey
    }

    fun computeSharedSecret(privateKey: XECPrivateKey, publicKey: XECPublicKey): ByteArray = KeyAgreement.getInstance("X25519").apply {
        init(privateKey)
        doPhase(publicKey, true)
    }.generateSecret()

}