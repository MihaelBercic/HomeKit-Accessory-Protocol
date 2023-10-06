package plugins.husqvarna.structure

import com.google.gson.annotations.SerializedName

data class HusqvarnaAuth(
    @SerializedName("access_token") val accessToken: String,
    val scope: String,
    @SerializedName("expires_in") val expiresIn: Int,
    @SerializedName("refresh_token") val refreshToken: String,
    val provider: String,
    @SerializedName("user_id") val userId: String,
    @SerializedName("token_type") val tokenType: String,
    var createdAt: Long = System.currentTimeMillis()
)