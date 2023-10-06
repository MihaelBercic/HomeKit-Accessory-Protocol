package homekit.tlv

/**
 * Created by Mihael Valentin Berčič
 * on 24/12/2020 at 11:36
 * using IntelliJ IDEA
 */
enum class PairingMethod(val typeValue: Byte) {
    PairSetup(0),
    PairSetupAuthentication(1),
    PairVerify(2),
    AddPairing(3),
    RemovePairing(4),
    ListPairings(5),
    None(-1);

    companion object {
        fun valueOf(byte: Byte) = values().firstOrNull { it.typeValue == byte } ?: None
    }
}
