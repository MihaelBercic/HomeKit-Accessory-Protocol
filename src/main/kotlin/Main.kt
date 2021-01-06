import homekit.HomeKitServer
import homekit.HomeKitService
import homekit.pairing.ChaCha
import javax.crypto.spec.ChaCha20ParameterSpec

/**
 * Created by Mihael Valentin Berčič
 * on 08/12/2020 at 22:41
 * using IntelliJ IDEA
 */


fun main() {
    HomeKitService().startAdvertising(30000)
    HomeKitServer()
}