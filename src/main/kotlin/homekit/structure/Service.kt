package homekit.structure

import com.google.gson.annotations.Expose
import homekit.communication.structure.AppleServices
import homekit.communication.structure.CharacteristicType

open class Service(
    @Expose val type: AppleServices,
    private val accessory: Accessory,
    @Expose val iid: Long,
    @Expose val primary: Boolean = false,
    @Expose val hidden: Boolean = false
) {

    @Expose
    private val characteristics = mutableListOf<Characteristic>()

    private fun alreadyExists(characteristicType: CharacteristicType) = characteristics.any { it.type == characteristicType }

    fun addCharacteristic(
        type: CharacteristicType,
        value: Any? = type.defaultValue,
        supportsEvents: Boolean = false,
        onChange: Characteristic.() -> Unit = {}
    ) = Characteristic(value, type, ((iid shl 8) or type.id.toLong()), onChange).apply {
        if (alreadyExists(type)) throw Exception("Characteristic with type $this@hasValue already exists.")
        characteristics.add(this)
        accessory.mappedCharacteristics[iid] = this
    }

}