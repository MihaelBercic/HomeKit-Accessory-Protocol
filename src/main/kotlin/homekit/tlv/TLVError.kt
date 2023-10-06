package homekit.tlv

/**
 * Created by Mihael Valentin Berčič
 * on 29/01/2021 at 10:20
 * using IntelliJ IDEA
 */
enum class TLVError(val code: Byte) {

    Unknown(0x01),
    Authentication(0x02),
    Backoff(0x03),
    MaxPeers(0x04),
    MaxTries(0x05),
    Unavailable(0x06),
    Busy(0x07)

}