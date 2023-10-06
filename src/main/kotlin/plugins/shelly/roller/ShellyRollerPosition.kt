package plugins.shelly.roller

import com.google.gson.annotations.SerializedName

/**
 * Created by Mihael Valentin Berčič
 * on 07/04/2021 at 12:51
 * using IntelliJ IDEA
 */
enum class PositionStates {
    @SerializedName("stop")
    Stopped,

    @SerializedName("close")
    Closing,

    @SerializedName("open")
    Opening
}