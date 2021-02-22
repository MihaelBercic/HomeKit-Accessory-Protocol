package homekit.communication.structure.data

import com.google.gson.annotations.SerializedName

/**
 * Created by Mihael Valentin Berčič
 * on 14/02/2021 at 12:46
 * using IntelliJ IDEA
 */

data class ChangeRequest(
    val aid: Int,
    val iid: Int,
    val value: Any?,
    val authData: String?,
    val remote: Boolean = false,
    @SerializedName("ev") val events: Boolean = false,
    @SerializedName("r") val response: Boolean = false
)

data class ChangeRequests(val characteristics: List<ChangeRequest>)