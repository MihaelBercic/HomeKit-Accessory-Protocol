package homekit

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class Configuration {

    @Expose
    @SerializedName("accessories")
    val accessoryData: List<Map<String, Any>> = emptyList()

    internal infix fun Map<String, Any>.getString(name: String) = get(name) as? String ?: throw Exception("Map does not contain the key.")
    internal infix fun Map<String, Any>.getInteger(name: String) = (get(name) as? Double)?.toInt() ?: throw Exception("Map does not contain the key.")

}