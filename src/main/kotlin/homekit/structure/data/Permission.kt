package homekit.structure.data

import com.google.gson.annotations.SerializedName

/**
 * Created by Mihael Valentin Berčič
 * on 05/02/2021 at 00:48
 * using IntelliJ IDEA
 */
enum class Permission {
    @SerializedName("aa")
    AdditionalAuthorization,

    @SerializedName("ev")
    Notify,

    @SerializedName("pr")
    PairedRead,

    @SerializedName("pw")
    PairedWrite,

    @SerializedName("tw")
    TimedWrite,

    @SerializedName("wr")
    WriteResponse
}