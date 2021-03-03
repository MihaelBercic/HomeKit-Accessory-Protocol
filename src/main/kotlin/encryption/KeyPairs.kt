package encryption

import java.security.interfaces.EdECPrivateKey
import java.security.interfaces.EdECPublicKey
import java.security.interfaces.XECPrivateKey
import java.security.interfaces.XECPublicKey

/**
 * Created by Mihael Valentin Berčič
 * on 03/03/2021 at 11:58
 * using IntelliJ IDEA
 */
data class CurveKeyPair(val privateKey: XECPrivateKey, val publicKey: XECPublicKey)
data class EdEcKeyPair(val privateKey: EdECPrivateKey, val publicKey: EdECPublicKey)