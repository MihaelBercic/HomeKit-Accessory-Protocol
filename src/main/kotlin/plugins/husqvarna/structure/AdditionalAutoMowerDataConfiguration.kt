package plugins.husqvarna.structure

import com.google.gson.annotations.SerializedName

data class AdditionalAutoMowerDataConfiguration(
    @SerializedName("client-id") val clientId: String,
    @SerializedName("client-secret") val clientSecret: String,
    val id: String,
)
