package homekit.communication.structure

import Logger
import com.google.gson.annotations.SerializedName
import gson
import homekit.communication.structure.data.ChangeRequest
import homekit.communication.structure.data.CharacteristicResponse
import shelly.ShellyBulbStatus
import java.lang.Integer.max
import java.lang.Integer.min
import java.net.URL
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

/**
 * Created by Mihael Valentin Berčič
 * on 17/02/2021 at 16:56
 * using IntelliJ IDEA
 */


class Bridge : Accessory(1) {

    override fun setup(configurationDetails: Map<String, Any>) {
        registerInformation("Bridge", "1.0.0", "1.0.0", "Bridge", "Mihael", "M1H43L") {
            Logger.info("Identifying bridge!")
        }

        service(2, AppleServices.ProtocolInformation) {
            addCharacteristic(CharacteristicType.Version, "1.1.0")
        }
    }

    override fun commitChanges(changeRequests: List<ChangeRequest>) {
        TODO("Not yet implemented")
    }

}

class Bulb(aid: Int) : Accessory(aid) {

    @Transient
    private val ip = "192.168.1.33"

    override
    fun setup(configurationDetails: Map<String, Any>) {
        service(2, AppleServices.LightBulb).apply {
            registerInformation("Bulb", "1.0.0", "1.0.0", "Mihael", "LightBulb", "ABCDEFG") {
                Logger.info("Identifying our light bulb!")
            }

            val status = try {
                gson.fromJson(URL("http://192.168.1.33/light/0").readText(), ShellyBulbStatus::class.java)
            } catch (e: Exception) {
                ShellyBulbStatus(true, 100, 0, 50)
            }

            addCharacteristic(CharacteristicType.On, status.isOn, supportsEvents = true) {
                actions["turn"] = if (value == true) "on" else "off"
            }

            addCharacteristic(CharacteristicType.Brightness, status.brightness) {
                if (value != null) actions["brightness"] = value!!
            }
            val kelvins = 1_000_000
            val currentTemperature = kelvins / status.temperature

            addCharacteristic(CharacteristicType.ColorTemperature, currentTemperature) {
                (value as? Int)?.apply {
                    actions["temp"] = min(6500, max(3000, kelvins / this))
                }
            }

        }
    }

    @Transient
    val executor = Executors.newSingleThreadScheduledExecutor()

    @Transient
    var scheduledFuture: ScheduledFuture<out Any>? = null

    override fun commitChanges(changeRequests: List<ChangeRequest>) {
        if (actions.isNotEmpty()) {
            val toSend = actions.map { (key, value) -> "$key=$value" }.joinToString("&")
            actions.clear()

            scheduledFuture?.cancel(true)
            scheduledFuture = executor.schedule({ URL("http://$ip/light/0?$toSend").readText() }, 500, TimeUnit.MILLISECONDS)
        }
    }

}

class Switch(aid: Int) : Accessory(aid) {

    data class RollerStats(
        @SerializedName("current_pos")
        val position: Int = 0
    )

    override fun setup(configurationDetails: Map<String, Any>) {

        registerInformation("Shelly Switch", "1.0.0", "1.0", "Shelly", "Switch", "Sh2lly") {

        }

        service(2, AppleServices.WindowCovering) {
            val stats = gson.fromJson(URL("http://192.168.1.150/roller/0").readText(), RollerStats::class.java)

            addCharacteristic(CharacteristicType.PositionState, supportsEvents = true)
            val currentPosition = addCharacteristic(CharacteristicType.CurrentPosition, stats.position, supportsEvents = true)

            addCharacteristic(CharacteristicType.TargetPosition, stats.position) {
                value?.apply {
                    currentPosition.value = this
                    actions["go"] = "to_pos"
                    actions["roller_pos"] = this
                }
            }

            /*
            addCharacteristic(CharacteristicType.HoldPosition) {
                if (value == 1) actions["go"] = "stop"
            }
            */
        }

    }

    @Transient
    val executor = Executors.newSingleThreadScheduledExecutor()

    @Transient
    var toSend: ScheduledFuture<out Any>? = null

    override fun commitChanges(changeRequests: List<ChangeRequest>) {
        val query = actions.map { "${it.key}=${it.value}" }.joinToString("&")
        toSend?.cancel(true)
        toSend = executor.schedule({ URL("http://192.168.1.150/roller/0?$query").readText() }, 1000, TimeUnit.MILLISECONDS)
    }

}

abstract class Accessory(val aid: Int) {

    private val services = mutableListOf<Service>()

    @Transient
    internal val actions = mutableMapOf<String, Any>()

    @Transient
    val mappedCharacteristics = hashMapOf<Int, Characteristic>()

    @Transient
    val eventMap = hashMapOf<Int, Characteristic.() -> Unit>()

    abstract fun setup(configurationDetails: Map<String, Any>)
    abstract fun commitChanges(changeRequests: List<ChangeRequest>)

    protected infix fun Characteristic.status(statusCodes: StatusCodes) = CharacteristicResponse(aid, iid, statusCodes.value)

    fun service(iid: Int, type: AppleServices, block: Service.() -> Unit = {}) = Service(type, this, (aid shl 8) or iid).apply {
        if (services.any { it.iid == iid }) throw Exception("Service with IID $iid already exists on this accessory.")
        apply(block)
        services.add(this)
    }

    operator fun get(iid: Int) = mappedCharacteristics[iid] ?: throw Exception("This characteristic does not exist.")

    internal fun registerInformation(name: String, firmware: String, version: String, manufacturer: String, model: String, serialNumber: String, onIdentify: Characteristic.() -> Unit) =
        service(1, AppleServices.AccessoryInformation).apply {
            addCharacteristic(CharacteristicType.Name, name)
            addCharacteristic(CharacteristicType.FirmwareRevision, firmware)
            addCharacteristic(CharacteristicType.Version, version)
            addCharacteristic(CharacteristicType.Manufacturer, manufacturer)
            addCharacteristic(CharacteristicType.Model, model)
            addCharacteristic(CharacteristicType.SerialNumber, serialNumber)
            addCharacteristic(CharacteristicType.Identify, onChange = onIdentify)
        }
}

open class Service(val type: AppleServices, @Transient val accessory: Accessory, val iid: Int, val primary: Boolean = false, val hidden: Boolean = false) {

    private val characteristics = mutableListOf<Characteristic>()

    private fun alreadyExists(characteristicType: CharacteristicType) = characteristics.any { it.type == characteristicType }

    fun addCharacteristic(type: CharacteristicType, value: Any? = type.defaultValue, supportsEvents: Boolean = false, onChange: Characteristic.() -> Unit = {}) =
        Characteristic(value, type, (iid shl 8) or type.id, onChange).apply {
            if (alreadyExists(type)) throw Exception("Characteristic with type $this@hasValue already exists.")
            characteristics.add(this)
            this.supportsEvents = supportsEvents
            accessory.mappedCharacteristics[iid] = this
        }

}

class Characteristic(value: Any? = null, val type: CharacteristicType, val iid: Int, @Transient val onChange: Characteristic.() -> Unit) {

    val format = type.format
    val perms = type.permissions
    val minValue = type.min
    val maxValue = type.max
    val step = type.step
    val unit = type.unit
    val maxLen: Any? = null
    var ev = false

    @Transient
    var supportsEvents = false

    var value: Any? = value
        set(value) {
            val newValue = when (value) {
                is Boolean, is Int, is String -> value
                is Double -> when (format) {
                    Format.Boolean -> value > 0
                    Format.Uint8, Format.Uint16, Format.Uint32, Format.Int -> value.toInt()
                    Format.Uint64 -> value.toLong()
                    Format.Float -> value.toFloat()
                    else -> throw Exception("Unknown DOUBLE type happened: [$format] $value")
                }
                else -> throw java.lang.Exception("Unknown type happened: $format ... $value")
            }
            if (!type.isReadOnly) {
                previousValue = field
                field = newValue
            }
            if (newValue != previousValue) onChange(this)
        }

    @Transient
    var previousValue: Any? = null

    private fun revert() {
        value = previousValue
    }

}