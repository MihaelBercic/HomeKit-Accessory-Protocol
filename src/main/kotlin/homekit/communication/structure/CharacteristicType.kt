package homekit.communication.structure

import com.google.gson.annotations.SerializedName

/**
 * Created by Mihael Valentin Berčič
 * on 14/02/2021 at 12:48
 * using IntelliJ IDEA
 */
enum class CharacteristicType(
    val defaultValue: Any?,
    val format: Format,
    val id: Int,
    vararg val permissions: Permission,
    val unit: CharacteristicUnit? = null,
    val min: Any? = null,
    val max: Any? = null,
    val step: Int? = null
) {

    @SerializedName("37")
    Version("1.1", Format.String, 0x37, Permission.PairedRead),

    @SerializedName("52")
    FirmwareRevision("1.0.0", Format.String, 0x52, Permission.PairedRead),

    @SerializedName("14")
    Identify(null, Format.Boolean, 0x14, Permission.PairedWrite),

    @SerializedName("20")
    Manufacturer("Mihael", Format.String, 0x20, Permission.PairedRead),

    @SerializedName("21")
    Model("DefaultModel", Format.String, 0x21, Permission.PairedRead),

    @SerializedName("23")
    Name("Default Name", Format.String, 0x23, Permission.PairedRead),

    @SerializedName("30")
    SerialNumber("Unknown", Format.String, 0x30, Permission.PairedRead),

    @SerializedName("25")
    On(false, Format.Boolean, 0x25, Permission.Notify, Permission.PairedRead, Permission.PairedWrite),

    @SerializedName("13")
    Hue(0, Format.Float, 0x13, Permission.Notify, Permission.PairedRead, Permission.PairedWrite, unit = CharacteristicUnit.ArcDegree, min = 0, max = 360),

    @SerializedName("2F")
    Saturation(0, Format.Float, 0x2F, Permission.Notify, Permission.PairedRead, Permission.PairedWrite, unit = CharacteristicUnit.Percentage, min = 0, max = 100),

    @SerializedName("8")
    Brightness(0, Format.Int, 0x8, Permission.Notify, Permission.PairedRead, Permission.PairedWrite, unit = CharacteristicUnit.Percentage, min = 0, max = 100),

    @SerializedName("CE")
    ColorTemperature(50, Format.Uint32, 0xCE, Permission.Notify, Permission.PairedRead, Permission.PairedWrite, min = 50, max = 400),

    @SerializedName("7C")
    TargetPosition(0, Format.Uint8, 0x7C, Permission.Notify, Permission.PairedRead, Permission.PairedWrite, unit = CharacteristicUnit.Percentage, min = 0, max = 100),

    @SerializedName("6D")
    CurrentPosition(0, Format.Uint8, 0x6D, Permission.Notify, Permission.PairedRead, unit = CharacteristicUnit.Percentage, min = 0, max = 100),

    @SerializedName("72")
    PositionState(2, Format.Uint8, 0x72, Permission.Notify, Permission.PairedRead, unit = CharacteristicUnit.Percentage, min = 0, max = 2),

    @SerializedName("6F")
    HoldPosition(null, Format.Boolean, 0x6F, Permission.PairedWrite)

    ;

    val isReadOnly = permissions.size == 1 && permissions[0] == Permission.PairedRead
}