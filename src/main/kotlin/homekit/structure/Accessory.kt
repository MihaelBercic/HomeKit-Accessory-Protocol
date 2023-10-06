package homekit.structure

import com.google.gson.annotations.Expose
import homekit.structure.data.CharacteristicType
import homekit.structure.data.ServiceType
import utils.HttpMethod
import utils.urlRequest

/**
 * Created by Mihael Valentin Berčič
 * on 14/02/2021 at 13:48
 * using IntelliJ IDEA
 * This class represents the physical accessory such as lamps, rollers, switches and so on.
 *
 * In this class you setup the interaction with the physical devices.
 *
 * @property aid Accessory ID provided by the user in the configuration file.
 * @property ip Accessory IP address provided by the user in the configuration file.
 */
abstract class Accessory(@Expose val aid: Long, val name: String, val ip: String = "") {

    @Expose
    private val services = mutableListOf<Service>()
    private val mappedServices = mutableMapOf<Long, Service>()
    val mappedCharacteristics = hashMapOf<Long, Characteristic>()

    /**
     * Setup is called on initial launch of the bridge in order to setup the accessory with their services and characteristics.
     *
     * In this method it is recommended to setup any events on the accessories themselves.
     *
     * @see [Characteristic]
     * @see [CharacteristicType]
     * @see [Service]
     * @see [ServiceType]
     *
     * @param configurationDetails Any custom information specified by the user in the Configuration file.
     * @param bridgeAddress IP address of the bridge itself. It is recommended for it to be static due to accessories sending events back to the static ip.
     */
    abstract fun setup(configurationDetails: Map<String, Any> = emptyMap(), bridgeAddress: String)

    /**
     * [update] is called on initial launch of the bridge as well as after events regarding a specific accessory have been triggered.
     *
     */
    abstract fun update()

    /**
     * [commitChanges] is called after any change to any of the characteristics.
     *
     * In this function you will commit any change requests to the accessories themselves.
     *
     * Make sure to not send any type of requests when they're not needed (aka empty POST or empty queries).
     */
    abstract fun commitChanges()

    fun addService(id: Long, type: ServiceType, block: Service.() -> Unit = {}) = Service(type, this, ((aid shl 7) or id)).apply {
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
    fun getService(serviceId: Long, block: Service.() -> Unit) = mappedServices[serviceId]?.apply(block)
        ?: throw Exception("Such service [$serviceId] does not exist!")

    fun sendRequest(type: HttpMethod, path: String, body: String = "", callback: (code: Int, body: String) -> Unit = { _, _ -> }) = urlRequest(type, "http://$ip$path", body, callback)

    operator fun get(iid: Long) = mappedCharacteristics[iid] ?: throw Exception("This characteristic does not exist.")

    internal fun registerInformation(firmware: String, version: String, manufacturer: String, model: String, serialNumber: String, onIdentify: Characteristic.() -> Unit = {}) =
        addService(1, ServiceType.AccessoryInformation).apply {
            add(CharacteristicType.Name, name)
            add(CharacteristicType.FirmwareRevision, firmware)
            add(CharacteristicType.Version, version)
            add(CharacteristicType.Manufacturer, manufacturer)
            add(CharacteristicType.Model, model)
            add(CharacteristicType.SerialNumber, serialNumber)
            add(CharacteristicType.Identify, onChange = onIdentify)
        }
}