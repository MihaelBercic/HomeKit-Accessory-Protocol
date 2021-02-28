package shelly

import com.google.gson.annotations.SerializedName

/**
 * Created by Mihael Valentin Berčič
 * on 26/02/2021 at 17:02
 * using IntelliJ IDEA
 */
data class ShellyBulbStatus(
    @SerializedName("ison") val isOn: Boolean,
    val brightness: Int,
    val white: Int,
    @SerializedName("temp")val temperature: Int
)