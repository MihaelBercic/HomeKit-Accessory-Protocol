package homekit.communication.structure

import Logger
import homekit.Device
import homekit.communication.structure.data.ChangeRequest
import homekit.communication.structure.data.CharacteristicResponse

/**
 * Created by Mihael Valentin Berčič
 * on 17/02/2021 at 16:56
 * using IntelliJ IDEA
 */


class Bridge : Accessory(1) {

    override fun setup(configurationDetails: Map<String, Any>) {
        val accessoryInformation = service(1, AppleServices.AccessoryInformation) {
            has(CharacteristicType.Name, "Bridge")
            has(CharacteristicType.Model, "Bridge")
            has(CharacteristicType.Version, "1.0.0")
            has(CharacteristicType.Manufacturer, "Mihael")
            has(CharacteristicType.SerialNumber, "ToGetFromJSON")
            has(CharacteristicType.FirmwareRevision)
            has(CharacteristicType.Identify)
        }
        val protocolInformation = service(2, AppleServices.ProtocolInformation) {
            has(CharacteristicType.Version, "1.1.0")
        }
    }

    override fun update() {}

    override fun commitChanges(device: Device, changeRequests: List<ChangeRequest>) {
        TODO("Not yet implemented")
    }

}

class Bulb(aid: Int) : Accessory(aid) {

    val info = service(1, AppleServices.AccessoryInformation) {
        has(CharacteristicType.Name, "Bulb")
        has(CharacteristicType.FirmwareRevision)
        has(CharacteristicType.Version, "69.6.9")
        has(CharacteristicType.Manufacturer, "Mihael")
        has(CharacteristicType.Model, "Bulb")
        has(CharacteristicType.SerialNumber, "ToGetFromJSON")
        has(CharacteristicType.Identify) { oldValue, newValue ->
            Logger.info("Identifying our bulb!")
        }
    }

    val lightBulbService = service(2, AppleServices.LightBulb) {
        has(CharacteristicType.On) { oldValue, newValue ->
            Logger.trace("ON was changed $oldValue -> $newValue")
        }
        has(CharacteristicType.Hue) { oldValue, newValue ->
            Logger.info("Hue was changed $oldValue -> $newValue")
        }
        has(CharacteristicType.Saturation) { oldValue, newValue ->
            Logger.info("Saturation was changed $oldValue -> $newValue")
        }
    }

    override fun setup(configurationDetails: Map<String, Any>) {

    }

    override fun update() {
        Logger.info("Updating our bulb status!")
    }

    override fun commitChanges(device: Device, changeRequests: List<ChangeRequest>) {
        Logger.info("Commiting changes...")
    }

}

abstract class Accessory(val aid: Int) {

    private val services = mutableListOf<Service>()

    @Transient
    private val mappedCharacteristics = hashMapOf<Int, Characteristic>()

    abstract fun setup(configurationDetails: Map<String, Any>)
    abstract fun update()
    abstract fun commitChanges(device: Device, changeRequests: List<ChangeRequest>)

    protected infix fun Characteristic.status(statusCodes: StatusCodes) = CharacteristicResponse(aid, iid, statusCodes.value)

    fun service(iid: Int, type: AppleServices, block: Service.() -> Unit) {
        val serviceID = (aid shl 8) or iid
        val service = Service(type, serviceID).apply(block)
        services.add(service)
        mappedCharacteristics.putAll(service.characteristics.map { it.iid to it })
    }

    operator fun get(iid: Int) = mappedCharacteristics[iid] ?: throw Exception("This characteristic does not exist.")

}

open class Service(val type: AppleServices, val iid: Int) {

    val characteristics = mutableListOf<Characteristic>()

    open val primary = false
    open val hidden = false

    @Transient
    val mappedCharacteristics = hashMapOf<CharacteristicType, Characteristic>()

    fun has(
        type: CharacteristicType,
        value: Any? = type.defaultValue,
        supportsEvents: Boolean = false,
        block: (oldValue: Any?, newValue: Any?) -> Unit = { _, _ -> }
    ) {
        val iid = (iid shl 8) or type.id
        val characteristic = Characteristic(value, type, iid, supportsEvents, block)
        mappedCharacteristics[type] = characteristic
        characteristics.add(characteristic)
    }

    infix fun CharacteristicType.set(value: Any?) {
        mappedCharacteristics[this]?.value = value ?: throw Exception("Non-existing characteristic type $this...")
    }

}

class Characteristic(
    value: Any? = null,
    val type: CharacteristicType,
    val iid: Int,
    @Transient val supportsEvents: Boolean,
    @Transient val onChange: (oldValue: Any?, newValue: Any?) -> Unit
) {

    val format: Format = type.format
    val perms = type.permissions
    val minValue: Any? = type.min
    val maxValue: Any? = type.max
    val step: Int? = type.step
    val unit: CharacteristicUnit? = type.unit
    val maxLen: Any? = null
    var ev = false

    var value: Any? = value
        set(value) {
            val newValue = when (value) {
                is Boolean -> value
                is Double -> when (format) {
                    Format.Boolean -> value > 0
                    Format.Uint8, Format.Uint16, Format.Uint32, Format.Int -> value.toInt()
                    Format.Uint64 -> value.toLong()
                    Format.Float -> value.toFloat()
                    else -> throw Exception("Unknown DOUBLE type happened: [$format] $value")
                }
                is String -> value
                else -> throw java.lang.Exception("Unknown type happened: $format ... $value")
            }
            onChange(field, newValue)
            previousValue = field
            field = newValue
        }

    @Transient
    private var previousValue: Any? = null

    private fun revert() {
        value = previousValue
    }

}