package homekit.encryption

import utils.asBigInteger
import utils.asByteArray
import utils.padded
import java.math.BigInteger
import java.security.MessageDigest
import java.security.SecureRandom
import kotlin.experimental.xor

/**
 * Created by Mihael Valentin Berčič
 * on 31/12/2020 at 12:33
 * using IntelliJ IDEA
 *
 * Class is used for Secure Remote Password homekit.encryption.
 *
 * Conforms to the following
 * @see <a href="https://tools.ietf.org/html/rfc2945">The SRP Authentication and Key Exchange System</a>
 * @see <a href="https://tools.ietf.org/html/rfc5054"> Using the Secure Remote Password (SRP) Protocol for TLS Authentication</a>
 */
class SRP {

    private val identifier = "Pair-Setup".toByteArray()
    private val secureRandom = SecureRandom()
    private val generator = BigInteger.valueOf(5)
    private val digest = MessageDigest.getInstance("SHA-512")

    val salt = ByteArray(16).apply { secureRandom.nextBytes(this) }

    private val prime = ("FFFFFFFFFFFFFFFFC90FDAA22168C234C4C6628B80DC1CD129024E088A67CC74020BBEA63B139B22" +
            "514A08798E3404DDEF9519B3CD3A431B302B0A6DF25F14374FE1356D6D51C245E485B576625E7EC6" +
            "F44C42E9A637ED6B0BFF5CB6F406B7EDEE386BFB5A899FA5AE9F24117C4B1FE649286651ECE45B3D" +
            "C2007CB8A163BF0598DA48361C55D39A69163FA8FD24CF5F83655D23DCA3AD961C62F356208552BB" +
            "9ED529077096966D670C354E4ABC9804F1746C08CA18217C32905E462E36CE3BE39E772C180E8603" +
            "9B2783A2EC07A28FB5C55DF06F4C52C9DE2BCBF6955817183995497CEA956AE515D2261898FA0510" +
            "15728E5A8AAAC42DAD33170D04507A33A85521ABDF1CBA64ECFB850458DBEF0A8AEA71575D060C7D" +
            "B3970F85A6E1E4C7ABF5AE8CDB0933D71E8C94E04A25619DCEE3D2261AD2EE6BF12FFA06D98A0864" +
            "D87602733EC86A64521F2B18177B200CBBE117577A615D6C770988C0BAD946E208E24FA074E5AB31" +
            "43DB5BFCE0FD108E4B82D120A93AD2CAFFFFFFFFFFFFFFFF").asBigInteger

    private val primeArray = prime.asByteArray
    private val generatorArray = generator.asByteArray
    private val paddedPrime = prime padded 384
    private val paddedGenerator = generator padded 384
    private val multiplier = digest.hash(*paddedPrime, *paddedGenerator).asBigInteger

    private lateinit var verifier: BigInteger
    private lateinit var privateKey: BigInteger
    private lateinit var publicKey: BigInteger
    private lateinit var sessionKey: BigInteger

    lateinit var sharedSecret: ByteArray

    /**
     * Computes SRP public key based on the [prime] and password (PIN) specified.
     *
     * @param password User entered PIN.
     * @return PublicKey as [BigInteger].
     */
    fun computePublicKey(password: String): BigInteger {
        val hashedCredentials = digest.hash(*identifier, ':'.toByte(), *password.toByteArray())
        val x = digest.hash(*salt, *hashedCredentials).asBigInteger
        verifier = generator.modPow(x, prime)
        privateKey = BigInteger(3072, secureRandom).mod(prime)
        publicKey = generator.modPow(privateKey, prime) + (multiplier.multiply(verifier)).mod(prime)
        return if (publicKey.asByteArray.size == 384) publicKey else computePublicKey(password)
    }

    /**
     * Verifies the client's public key sent by the client with the evidence (proof) the client sent.
     *
     * @param clientPublicKey
     * @param clientEvidence
     * @return Server evidence if verification was successful or BigInteger.Zero if not.
     */
    fun verifyDevice(clientPublicKey: BigInteger, clientEvidence: BigInteger): BigInteger {
        val paddedClientPublicKey = clientPublicKey padded 384
        val paddedPublicKey = publicKey padded 384
        val u = digest.hash(*paddedClientPublicKey, *paddedPublicKey).asBigInteger

        sessionKey = verifier.modPow(u, prime).multiply(clientPublicKey).modPow(privateKey, prime)

        val sessionKeyArray = sessionKey.asByteArray
        val clientPublicKeyArray = clientPublicKey.asByteArray
        val computedEvidence = computeClientEvidence(sessionKeyArray, clientPublicKeyArray)

        if (computedEvidence != clientEvidence) return BigInteger.ZERO
        return computeServerEvidence(sessionKeyArray, clientPublicKeyArray, clientEvidence.asByteArray)
    }

    /**
     * Computes client evidence from the SessionKey and Client's Public Key used for verification.
     *
     * @param sessionKeyArray current SRP session key.
     * @param clientPublicKey client's public key.
     * @return BigInteger that is representing client evidence.
     */
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

    /**
     * Computes server evidence (proof) for the current session.
     *
     * @param sessionKey current session's key.
     * @param clientPublicKey client's public key.
     * @param clientEvidence client's evidence (proof).
     * @return H(CPK | CE | HSK) as [BigInteger] which represents server evidence.
     */
    private fun computeServerEvidence(sessionKey: ByteArray, clientPublicKey: ByteArray, clientEvidence: ByteArray): BigInteger {
        val hashedSessionKey = digest.hash(*sessionKey)
        sharedSecret = hashedSessionKey
        return digest.hash(*clientPublicKey, *clientEvidence, *hashedSessionKey).asBigInteger
    }

    private fun MessageDigest.hash(vararg bytes: Byte): ByteArray {
        update(bytes)
        return digest()
    }

    private infix fun ByteArray.xor(b2: ByteArray): ByteArray {
        val result = ByteArray(size)
        for (i in indices) result[i] = (this[i] xor b2[i])
        return result
    }
}
