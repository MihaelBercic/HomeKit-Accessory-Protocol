package homekit.tlv

import homekit.tlv.data.Value
import homekit.tlv.structure.Item
import java.nio.ByteBuffer

/**
 * Created by Mihael Valentin Berčič
 * on 26/12/2020 at 01:27
 * using IntelliJ IDEA
 */
class FlagsItem {

    // TODO
    // 4 isTransient
    // 24 isSplit

    val isTransient: Boolean
    val isSplit: Boolean

    constructor(isTransient: Boolean, isSplit: Boolean) {
        this.isTransient = isTransient
        this.isSplit = isSplit
    }

    constructor(bytes: List<Byte>) {
        this.isTransient = true
        this.isSplit = true
    }

}