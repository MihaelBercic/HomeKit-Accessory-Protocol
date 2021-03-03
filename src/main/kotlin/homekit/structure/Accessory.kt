package homekit.structure

import com.google.gson.annotations.Expose
import homekit.communication.structure.AppleServices
import homekit.communication.structure.CharacteristicType
import homekit.communication.structure.data.ChangeRequest
import utils.NetworkRequestType
import utils.urlRequest

abstract class Accessory(@Expose val aid: Int, val ip: String = "") {

    @Expose
    private val services = mutableListOf<Service>()
    val mappedCharacteristics = hashMapOf<Long, Characteristic>()

    abstract fun setup(configurationDetails: Map<String, Any> = emptyMap())
    abstract fun commitChanges(changeRequests: List<ChangeRequest>)

    fun service(id: Int, type: AppleServices, block: Service.() -> Unit = {}) = Service(type, this, ((aid shl 8) or id).toLong()).apply {
        if (services.any { it.iid == iid }) throw Exception("Service with IID $id already exists on this accessory.")
        apply(block)
        services.add(this)
    }

    fun sendRequest(type: NetworkRequestType, path: String, body: String = "") = urlRequest(type, "http://$ip$path", body)

    operator fun get(iid: Long) = mappedCharacteristics[iid] ?: throw Exception("This characteristic does not exist.")

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