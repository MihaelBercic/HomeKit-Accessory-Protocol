package homekit.tlv

/**
 * Created by Mihael Valentin Berčič
 * on 24/12/2020 at 11:36
 * using IntelliJ IDEA
 */
enum class Tag(val typeValue: Int) {
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
    Permissions(0x0B),
    Flags(19),
    Separator(0xFF),
    None(-1);

    companion object {
        fun valueOf(byte: Byte) = values().firstOrNull { it.typeValue == byte.toInt() and 0xFF } ?: None
    }
}
