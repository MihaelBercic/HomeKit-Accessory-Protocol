package homekit.communication

import homekit.communication.LiveSessions.manageSubscription
import homekit.structure.data.ChangeRequests
import homekit.structure.data.CharacteristicResponse
import homekit.structure.data.CharacteristicsResponse
import homekit.structure.data.StatusCodes
import homekit.structure.storage.AccessoryStorage
import utils.Logger
import utils.appleGson
import utils.gson

/**
 * Created by Mihael Valentin Berčič
 * on 09/03/2021 at 00:39
 * using IntelliJ IDEA
 */
object Characteristics {

    /**
     * Retrieves all the characteristics that were requested for.
     *
     * @param query Http request query.
     * @param storage [AccessoryStorage].
     * @return Http response containing json made with the requested characteristics.
     */
    fun retrieve(query: String, storage: AccessoryStorage): HttpResponse {
        val querySplit = query.split("&")[0].replace("id=", "")
        val responses = querySplit.split(",").mapNotNull {
            val split = it.split(".")
            if (split.size == 2) {
                val aid = split[0].toLong()
                val cid = split[1].toLong()
                val accessory = storage[aid]
                val characteristic = accessory[cid]
                CharacteristicResponse(aid, cid, characteristic.value, status = StatusCodes.Success.value)
            } else null
        }
        return HttpResponse(207, data = appleGson.toJson(CharacteristicsResponse(responses)).toByteArray())
    }

    /**
     * Executes the requested characteristic changes.
     *
     * @param body Change requests encoded in JSON format.
     * @param storage [AccessoryStorage].
     * @param session Current [Session].
     * @return Http response containing each characteristics' status.
     *
     * @see [StatusCodes]
     */
    fun resolveChangeRequests(body: String, storage: AccessoryStorage, session: Session): HttpResponse {
        val changeRequests = gson.fromJson(body, ChangeRequests::class.java)
        val responses = mutableListOf<CharacteristicResponse>()

        changeRequests.characteristics.groupBy { it.aid }.forEach { (aid, requests) ->
            val accessory = storage[aid]
            requests.forEach { request ->
                val characteristic = accessory[request.iid]
                when {
                    request.events != null -> session.manageSubscription(characteristic, request.events)
                    request.value != null -> characteristic.value = request.value
                }
                responses.add(CharacteristicResponse(aid, request.iid, status = StatusCodes.Success.value))
            }
            accessory.commitChanges()
        }
        return HttpResponse(207, data = appleGson.toJson(CharacteristicsResponse(responses)).toByteArray())
    }

}