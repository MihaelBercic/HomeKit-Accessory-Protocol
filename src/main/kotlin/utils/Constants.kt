package utils

/**
 * Created by Mihael Valentin Berčič
 * on 05/02/2021 at 00:51
 * using IntelliJ IDEA
 */
object Constants {

    val encryptionSalt = "Pair-Setup-Encrypt-Salt".toByteArray()
    val encryptionInfo = "Pair-Setup-Encrypt-Info".toByteArray()

    val controllerSalt = "Pair-Setup-Controller-Sign-Salt".toByteArray()
    val controllerInfo = "Pair-Setup-Controller-Sign-Info".toByteArray()

    val accessorySignSalt = "Pair-Setup-Accessory-Sign-Salt".toByteArray()
    val accessorySignInfo = "Pair-Setup-Accessory-Sign-Info".toByteArray()

    val verifyEncryptSalt = "Pair-Verify-Encrypt-Salt".toByteArray()
    val verifyEncryptInfo = "Pair-Verify-Encrypt-Info".toByteArray()
    val accessoryKeyInfo = "Control-Read-Encryption-Key".toByteArray()
    val controllerKeyInfo = "Control-Write-Encryption-Key".toByteArray()
    val controlSalt = "Control-Salt".toByteArray()

}