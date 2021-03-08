package homekit.structure

import homekit.communication.structure.AppleServices
import homekit.communication.structure.CharacteristicType
import homekit.communication.structure.data.ChangeRequest

/**
 * Created by Mihael Valentin Berčič
 * on 17/02/2021 at 16:56
 * using IntelliJ IDEA
 */


class Bridge(ip: String) : Accessory(1) {

    init {
        setup(bridgeAddress = ip)
    }

    override fun setup(configurationDetails: Map<String, Any>, bridgeAddress: String) {
        registerInformation("Bridge", "1.0.0", "1.0.0", "Bridge", "Mihael", "M1H43L")
        addService(2, AppleServices.ProtocolInformation) {
            add(CharacteristicType.Version, "1.1.0")
        }
    }

    override fun update() {}
    override fun commitChanges(changeRequests: List<ChangeRequest>) {}

}