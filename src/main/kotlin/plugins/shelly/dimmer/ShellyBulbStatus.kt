package plugins.shelly.dimmer

import com.google.gson.annotations.SerializedName

/**
 * Created by Mihael Valentin Berčič
 * on 26/02/2021 at 17:02
 * using IntelliJ IDEA
 */
data class ShellyBulbStatus(
    val brightness: Int,
    val white: Int,
    @SerializedName("ison") val isOn: Boolean,
    @SerializedName("temp")val temperature: Int
)