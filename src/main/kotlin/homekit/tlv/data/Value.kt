package homekit.tlv.data

/**
 * Created by Mihael Valentin Berčič
 * on 24/12/2020 at 11:36
 * using IntelliJ IDEA
 */
enum class Value(val typeValue: Byte) {
    Method(0),
    Identifier(1),
    Salt(2),
    PublicKey(3),
    Proof(4),
    EncryptedData(5),
    State(6),
    Error(7),
    RetryDelay(8),
    Certificate(9),
    Signature(10),
    Flags(19),
    None(-1);

    companion object {
        fun valueOf(byte: Byte) = values().firstOrNull { it.typeValue == byte } ?: None
    }
}
