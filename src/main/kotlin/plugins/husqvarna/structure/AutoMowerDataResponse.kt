package plugins.husqvarna.structure

data class AutoMowerDataResponse(val data: AutoMowerData)

data class AutoMowerData(val type: String, val id: String, val attributes: MowerData)

data class MowerData(
    val battery: MowerBatteryData,
    val mower: MowerApp
)

data class MowerBatteryData(val batteryPercent: Int)
data class MowerApp(val mode: String, val state: String, val activity: String)