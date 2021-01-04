package homekit.pairing.srp

import asBigInteger
import asByteArray
import hash
import padded
import xor
import java.math.BigInteger
import java.security.MessageDigest
import java.security.SecureRandom

/**
 * Created by Mihael Valentin Berčič
 * on 31/12/2020 at 12:33
 * using IntelliJ IDEA
 */
class SRP {

    private val identifier = "Pair-Setup".toByteArray()
    private val secureRandom = SecureRandom()
    private val generator = BigInteger.valueOf(5)
    private val digest = MessageDigest.getInstance("SHA-512")

    private val prime =
        "FFFFFFFFFFFFFFFFC90FDAA22168C234C4C6628B80DC1CD129024E088A67CC74020BBEA63B139B22514A08798E3404DDEF9519B3CD3A431B302B0A6DF25F14374FE1356D6D51C245E485B576625E7EC6F44C42E9A637ED6B0BFF5CB6F406B7EDEE386BFB5A899FA5AE9F24117C4B1FE649286651ECE45B3DC2007CB8A163BF0598DA48361C55D39A69163FA8FD24CF5F83655D23DCA3AD961C62F356208552BB9ED529077096966D670C354E4ABC9804F1746C08CA18217C32905E462E36CE3BE39E772C180E86039B2783A2EC07A28FB5C55DF06F4C52C9DE2BCBF6955817183995497CEA956AE515D2261898FA051015728E5A8AAAC42DAD33170D04507A33A85521ABDF1CBA64ECFB850458DBEF0A8AEA71575D060C7DB3970F85A6E1E4C7ABF5AE8CDB0933D71E8C94E04A25619DCEE3D2261AD2EE6BF12FFA06D98A0864D87602733EC86A64521F2B18177B200CBBE117577A615D6C770988C0BAD946E208E24FA074E5AB3143DB5BFCE0FD108E4B82D120A93AD2CAFFFFFFFFFFFFFFFF".asBigInteger


    val salt = ByteArray(16).apply { secureRandom.nextBytes(this) }

    private val primeArray = prime.asByteArray
    private val generatorArray = generator.asByteArray
    private val paddedPrime = prime padded 384
    private val paddedGenerator = generator padded 384
    private val multiplier = digest.hash(*paddedPrime, *paddedGenerator).asBigInteger

    private lateinit var verifier: BigInteger
    private lateinit var privateKey: BigInteger
    private lateinit var publicKey: BigInteger
    lateinit var sessionKey: BigInteger

    private fun computePublicKey(): BigInteger = generator.modPow(privateKey, prime) + (multiplier.multiply(verifier)).mod(prime).let {
        if (it.asByteArray.size == 384) it else computePublicKey()
    }

    fun performFirstStep(password: String): BigInteger {
        val hashedCredentials = digest.hash(*identifier, ':'.toByte(), *password.toByteArray())
        val x = digest.hash(*salt, *hashedCredentials).asBigInteger
        verifier = generator.modPow(x, prime)
        privateKey = BigInteger(3072, secureRandom).mod(prime)
        publicKey = computePublicKey()
        return publicKey
    }

    fun performSecondStep(clientPublicKey: BigInteger, clientEvidence: BigInteger): BigInteger {
        val paddedClientPublicKey = clientPublicKey padded 384
        val paddedPublicKey = publicKey padded 384
        val u = digest.hash(*paddedClientPublicKey, *paddedPublicKey).asBigInteger

        sessionKey = verifier.modPow(u, prime).multiply(clientPublicKey).modPow(privateKey, prime)

        val sessionKeyArray = sessionKey.asByteArray
        val clientPublicKeyArray = clientPublicKey.asByteArray
        val computedEvidence = computeClientEvidence(sessionKeyArray, clientPublicKeyArray)

        if (computedEvidence != clientEvidence) throw Exception("Evidences do not match...")
        return computeServerEvidence(sessionKeyArray, clientPublicKeyArray, clientEvidence.asByteArray)
    }

    private fun computeClientEvidence(sessionKeyArray: ByteArray, clientPublicKey: ByteArray): BigInteger {
        val hashedPrime = digest.hash(*primeArray)
        val hashedGenerator = digest.hash(*generatorArray)
        val xor = hashedPrime xor hashedGenerator
        val hashedIdentifier = digest.hash(*identifier)
        val hashedSessionKey = digest.hash(*sessionKeyArray)
        return digest.hash(
            *xor,
            *hashedIdentifier,
            *salt,
            *clientPublicKey,
            *publicKey.asByteArray,
            *hashedSessionKey
        ).asBigInteger
    }

    private fun computeServerEvidence(sessionKey: ByteArray, clientPublicKey: ByteArray, clientEvidence: ByteArray): BigInteger {
        val hashedSessionKey = digest.hash(*sessionKey)
        return digest.hash(*clientPublicKey, *clientEvidence, *hashedSessionKey).asBigInteger
    }
}
