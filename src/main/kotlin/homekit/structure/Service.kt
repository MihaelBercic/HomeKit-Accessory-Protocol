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

    private val characteristicMap = mutableMapOf<CharacteristicType, Characteristic>()

    private fun alreadyExists(characteristicType: CharacteristicType) = characteristicMap[characteristicType] != null

    fun set(type: CharacteristicType, value: () -> Any?) {
        characteristicMap[type]?.value = value()
            ?: throw Exception("Characteristic with $type does not exist in this service: $type")
    }

    fun add(type: CharacteristicType, value: Any? = type.defaultValue, onChange: Characteristic.() -> Unit = {}) =
        Characteristic(value, type, ((iid shl 8) or type.id.toLong()), onChange).apply {
            if (alreadyExists(type)) throw Exception("Characteristic with type $this@hasValue already exists.")
            characteristics.add(this)
            characteristicMap[type] = this
            accessory.mappedCharacteristics[iid] = this
        }

}