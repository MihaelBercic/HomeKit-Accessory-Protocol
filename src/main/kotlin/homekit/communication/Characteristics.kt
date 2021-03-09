package homekit.communication

import homekit.communication.structure.StatusCodes
import homekit.communication.structure.data.AccessoryStorage
import homekit.communication.structure.data.CharacteristicResponse
import homekit.communication.structure.data.CharacteristicsResponse
import utils.appleGson

/**
 * Created by Mihael Valentin Berčič
 * on 09/03/2021 at 00:39
 * using IntelliJ IDEA
 */
object Characteristics {

    fun retrieve(query: String, storage: AccessoryStorage): HttpResponse {
        val querySplit = query.split("&")[0].replace("id=", "")
        val responses = querySplit.split(",")
            .map {
                val split = it.split(".")
                val aid = split[0].toInt()
                val cid = split[1].toLong()
                val accessory = storage[aid]
                val characteristic = accessory[cid]
                CharacteristicResponse(aid, cid, characteristic.value, status = StatusCodes.Success.value)
            }
        return HttpResponse(207, data = appleGson.toJson(CharacteristicsResponse(responses)).toByteArray())
    }
}