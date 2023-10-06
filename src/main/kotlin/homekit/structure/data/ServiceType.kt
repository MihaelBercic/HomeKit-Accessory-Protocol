package homekit.structure.data

import com.google.gson.annotations.SerializedName

/**
 * Created by Mihael Valentin Berčič
 * on 14/02/2021 at 12:41
 * using IntelliJ IDEA
 */
enum class ServiceType(val value: Int) {

    @SerializedName("3E")
    AccessoryInformation(0x3E),

    @SerializedName("A2")
    ProtocolInformation(0xA2),

    @SerializedName("43")
    LightBulb(0x43),

    @SerializedName("8C")
    WindowCovering(0x8C),

    @SerializedName("96")
    Battery(0x96),

    @SerializedName("B7")
    Fan(0xB7),

}