package homekit.structure.data

import com.google.gson.annotations.SerializedName

/**
 * Created by Mihael Valentin Berčič
 * on 28/01/2021 at 16:58
 * using IntelliJ IDEA
 */
enum class CharacteristicUnit {

    @SerializedName("celsius")
    Celsius,

    @SerializedName("percentage")
    Percentage,

    @SerializedName("arcdegrees")
    ArcDegree,

    @SerializedName("lux")
    Lux,

    @SerializedName("seconds")
    Seconds
}