package homekit.communication.structure.data

import com.google.gson.annotations.SerializedName
import homekit.communication.structure.CharacteristicUnit
import homekit.communication.structure.Format
import homekit.communication.structure.Permission

/**
 * Created by Mihael Valentin Berčič
 * on 14/02/2021 at 12:45
 * using IntelliJ IDEA
 */
data class CharacteristicResponse(
    var aid: Int,
    var iid: Int,
    var value: Any? = null,
    var format: Format? = null,
    var unit: CharacteristicUnit? = null,
    var perms: List<Permission>? = null,
    var type: String? = null,
    @SerializedName("ev") val events: Boolean? = null,
    var minValue: Any? = null,
    var maxValue: Any? = null,
    var minStep: Any? = null,
    var maxLen: Any? = null
)

data class CharacteristicsResponse(val characteristics: List<CharacteristicResponse>)