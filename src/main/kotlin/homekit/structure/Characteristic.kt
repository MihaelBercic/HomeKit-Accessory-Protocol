package homekit.structure

import com.google.gson.annotations.Expose
import homekit.communication.LiveSessions.isCurrentSessionSubscribed
import homekit.structure.data.CharacteristicType
import homekit.structure.data.Format

/**
 * This class holds information about an individual characteristic and allows
 *
 * @property type [CharacteristicType] of the characteristic.
 * @property iid Characteristic unique identifier.
 * @property onChange Block to execute on value change.
 *
 * @constructor
 * @param value to hold upon creation of the characteristic.
 */
class Characteristic(value: Any?, @Expose val type: CharacteristicType, @Expose val iid: Long, val onChange: Characteristic.() -> Unit) {

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
    var ev: Boolean = isCurrentSessionSubscribed

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
            val previousValue = field
            if (!type.isReadOnly) field = newValue
            if (newValue != previousValue) onChange(this)
        }
}