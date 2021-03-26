package homekit.structure.data

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

/**
 * Created by Mihael Valentin Berčič
 * on 14/02/2021 at 12:45
 * using IntelliJ IDEA
 */
data class CharacteristicResponse(
    @Expose val aid: Long,
    @Expose val iid: Long,
    @Expose val value: Any? = null,
    @Expose val format: Format? = null,
    @Expose val unit: CharacteristicUnit? = null,
    @Expose val perms: List<Permission>? = null,
    @Expose val type: String? = null,
    @Expose @SerializedName("ev") val events: Boolean? = null,
    @Expose val minValue: Any? = null,
    @Expose val maxValue: Any? = null,
    @Expose val minStep: Any? = null,
    @Expose val maxLen: Any? = null,
    @Expose val status: Int = StatusCodes.Success.value
)

data class CharacteristicsResponse(@Expose val characteristics: List<CharacteristicResponse>)