package homekit

import homekit.structure.Accessory
import homekit.structure.data.ServiceType
import homekit.structure.data.CharacteristicType

/**
 * Created by Mihael Valentin Berčič
 * on 17/02/2021 at 16:56
 * using IntelliJ IDEA
 *
 * This class is a representation of the bridge itself.
 *
 * It has a Protocol Information with version 1.1.0, that stands for Internet Protocol.
 */
class Bridge(ip: String) : Accessory(1) {

    init {
        setup(bridgeAddress = ip)
    }

    override fun setup(configurationDetails: Map<String, Any>, bridgeAddress: String) {
        registerInformation("Bridge", "1.0.0", "1.0.0", "Bridge", "Mihael", "M1H43L")
        addService(2, ServiceType.ProtocolInformation) {
            add(CharacteristicType.Version, "1.1.0")
        }
    }

    override fun update() {}
    override fun commitChanges() {}

}