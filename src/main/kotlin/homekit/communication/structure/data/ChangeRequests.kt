package homekit.communication.structure.data

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

/**
 * Created by Mihael Valentin Berčič
 * on 14/02/2021 at 12:46
 * using IntelliJ IDEA
 */

data class ChangeRequest(
    @Expose val aid: Int,
    @Expose val iid: Long,
    @Expose val value: Any?,
    @Expose val authData: String?,
    @Expose val remote: Boolean?,
    @Expose @SerializedName("ev") val events: Boolean?,
    @Expose @SerializedName("r") val response: Boolean?
)

data class ChangeRequests(@Expose val characteristics: List<ChangeRequest>)