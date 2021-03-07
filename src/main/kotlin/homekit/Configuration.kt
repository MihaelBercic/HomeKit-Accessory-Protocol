package homekit

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class Configuration {

    @Expose
    @SerializedName("accessories")
    val accessoryData: List<Map<String, Any>> = emptyList()

}