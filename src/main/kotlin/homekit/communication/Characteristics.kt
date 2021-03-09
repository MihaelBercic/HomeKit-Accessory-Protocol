package homekit.communication

import homekit.communication.LiveSessions.manageSubscription
import homekit.structure.data.StatusCodes
import homekit.structure.storage.AccessoryStorage
import homekit.structure.data.ChangeRequests
import homekit.structure.data.CharacteristicResponse
import homekit.structure.data.CharacteristicsResponse
import utils.appleGson
import utils.gson

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

    fun resolveChangeRequests(body: String, storage: AccessoryStorage, session: Session): HttpResponse {
        val changeRequests = gson.fromJson(body, ChangeRequests::class.java)
        val responses = mutableListOf<String>()

        changeRequests.characteristics.groupBy { it.aid }.forEach { (aid, requests) ->
            val accessory = storage[aid]
            requests.map { request ->
                val characteristic = accessory[request.iid]
                when {
                    request.events != null -> session.manageSubscription(characteristic, request.events)
                    request.value != null -> characteristic.value = request.value
                }
                CharacteristicResponse(aid, request.iid, status = 0)
            }
            accessory.commitChanges()
        }
        return HttpResponse(207, data = "{\"characteristics\":[${responses.joinToString(",")}]}".toByteArray())
    }

}