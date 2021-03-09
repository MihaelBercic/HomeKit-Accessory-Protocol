package homekit.structure

import com.google.gson.annotations.Expose
import homekit.structure.data.AppleServices
import homekit.structure.data.CharacteristicType
import utils.NetworkRequestType
import utils.urlRequest

abstract class Accessory(@Expose val aid: Int, val ip: String = "") {

    @Expose
    private val services = mutableListOf<Service>()
    private val mappedServices = mutableMapOf<Int, Service>()
    val mappedCharacteristics = hashMapOf<Long, Characteristic>()

    abstract fun setup(configurationDetails: Map<String, Any> = emptyMap(), bridgeAddress: String)
    abstract fun update()
    abstract fun commitChanges()

    fun addService(id: Int, type: AppleServices, block: Service.() -> Unit = {}) = Service(type, this, ((aid shl 8) or id).toLong()).apply {
        if (services.any { it.iid == iid }) throw Exception("Service with IID $id already exists on this accessory.")
        apply(block)
        mappedServices[id] = this
        services.add(this)
    }

    /**
     * Returns a service with the specified ID if it exists.
     *
     *
     * @param serviceId Service ID to retrieve from this accessory.
     * @param block A callback what to do after successful service retrieval.
     */
    fun getService(serviceId: Int, block: Service.() -> Unit) = mappedServices[serviceId]?.apply(block)
        ?: throw Exception("Such service [$serviceId] does not exist!")

    fun sendRequest(type: NetworkRequestType, path: String, body: String = "", callback: (code: Int, body: String) -> Unit = { _, _ -> }) = urlRequest(type, "http://$ip$path", body, callback)

    operator fun get(iid: Long) = mappedCharacteristics[iid] ?: throw Exception("This characteristic does not exist.")

    internal fun registerInformation(name: String, firmware: String, version: String, manufacturer: String, model: String, serialNumber: String, onIdentify: Characteristic.() -> Unit = {}) =
        addService(1, AppleServices.AccessoryInformation).apply {
            add(CharacteristicType.Name, name)
            add(CharacteristicType.FirmwareRevision, firmware)
            add(CharacteristicType.Version, version)
            add(CharacteristicType.Manufacturer, manufacturer)
            add(CharacteristicType.Model, model)
            add(CharacteristicType.SerialNumber, serialNumber)
            add(CharacteristicType.Identify, onChange = onIdentify)
        }
}