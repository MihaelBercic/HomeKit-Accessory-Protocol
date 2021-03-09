package homekit.structure.data

import com.google.gson.annotations.SerializedName

/**
 * Created by Mihael Valentin Berčič
 * on 05/02/2021 at 00:50
 * using IntelliJ IDEA
 */
enum class Format {

    @SerializedName("bool")
    Boolean,

    @SerializedName("uint8")
    Uint8,

    @SerializedName("uint16")
    Uint16,

    @SerializedName("uint32")
    Uint32,

    @SerializedName("uint64")
    Uint64,

    @SerializedName("int")
    Int,

    @SerializedName("float")
    Float,

    @SerializedName("string")
    String,

    @SerializedName("tlv8")
    TLV8,

    @SerializedName("data")
    Data

}