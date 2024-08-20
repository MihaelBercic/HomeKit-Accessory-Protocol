package plugins.shelly.roller

import com.google.gson.annotations.SerializedName

/**
 * Created by Mihael Valentin Berčič
 * on 03/03/2021 at 12:49
 * using IntelliJ IDEA
 */
data class ShellyCoverStatus(
    @SerializedName("current_pos")
    val position: Int = 0,
    val state: PositionStates
)