package homekit.structure

import utils.Logger
import homekit.communication.structure.AppleServices
import homekit.communication.structure.CharacteristicType
import homekit.communication.structure.data.ChangeRequest

/**
 * Created by Mihael Valentin Berčič
 * on 17/02/2021 at 16:56
 * using IntelliJ IDEA
 */


class Bridge : Accessory(1) {

    init {
        setup()
    }

    override fun setup(configurationDetails: Map<String, Any>) {
        registerInformation("Bridge", "1.0.0", "1.0.0", "Bridge", "Mihael", "M1H43L") {
            Logger.info("Identifying bridge!")
        }

        service(2, AppleServices.ProtocolInformation) {
            addCharacteristic(CharacteristicType.Version, "1.1.0")
        }
    }

    override fun commitChanges(changeRequests: List<ChangeRequest>) {
        TODO("Not yet implemented")
    }

}