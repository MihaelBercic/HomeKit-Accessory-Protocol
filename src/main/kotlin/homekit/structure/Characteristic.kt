package homekit.structure

import com.google.gson.annotations.Expose
import homekit.communication.structure.CharacteristicType
import homekit.communication.structure.Format

class Characteristic(value: Any? = null, @Expose val type: CharacteristicType, @Expose val iid: Long, val onChange: Characteristic.() -> Unit) {

    @Expose
    val format = type.format

    @Expose
    val perms = type.permissions

    @Expose
    val minValue = type.min

    @Expose
    val maxValue = type.max

    @Expose
    val step = type.step

    @Expose
    val unit = type.unit

    @Expose
    val maxLen: Any? = null

    @Expose
    var ev = false

    @Expose
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

    var previousValue: Any? = null
    var supportsEvents = false

    private fun revert() {
        value = previousValue
    }

}