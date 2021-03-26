package homekit.structure

import com.google.gson.annotations.Expose
import homekit.structure.data.CharacteristicType
import homekit.structure.data.ServiceType
import utils.Logger

open class Service(
    @Expose val type: ServiceType,
    private val accessory: Accessory,
    @Expose val iid: Long,
    @Expose val primary: Boolean = false,
    @Expose val hidden: Boolean = false
) {

    @Expose
    private val characteristics = mutableListOf<Characteristic>()
    private val characteristicMap = mutableMapOf<CharacteristicType, Characteristic>()

    private fun alreadyExists(characteristicType: CharacteristicType) = characteristicMap[characteristicType] != null

    /**
     * Fetches the characteristic with the given type.
     *
     * If the characteristic exists, it's value is changed. If not, an exception is thrown that such characteristic does not exist.
     *
     * @param type [CharacteristicType] of a characteristic.
     * @param value Value to be set if the characteristic exists.
     */
    fun set(type: CharacteristicType, value: () -> Any?) {
        characteristicMap[type]?.value = value() ?: throw Exception("Characteristic with $type does not exist in this service: $type")
    }

    /**
     * Adds the characteristic with the given [CharacteristicType] to the service.
     *
     * @param type [CharacteristicType]
     * @param value [Any] value to be set on creation.
     * @param onChange Block to run on value change.
     */
    fun add(type: CharacteristicType, value: Any? = type.defaultValue, onChange: Characteristic.() -> Unit = {}) =
        Characteristic(value, type, (iid.shl(8).or(type.id.toLong())), onChange).apply {
            if (alreadyExists(type)) throw Exception("Characteristic with type $this@hasValue already exists.")
            characteristics.add(this)
            characteristicMap[type] = this
            accessory.mappedCharacteristics[iid]?.apply {
                Logger.error("ALREADY EXISTS!")
            }
            accessory.mappedCharacteristics[iid] = this
        }

}