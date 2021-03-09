package homekit.communication

import homekit.communication.LiveSessions.subscribedSessions
import homekit.structure.storage.AccessoryStorage
import homekit.structure.data.CharacteristicResponse
import homekit.structure.data.CharacteristicsResponse
import utils.ResponseType
import utils.appleGson

/**
 * Created by Mihael Valentin Berčič
 * on 09/03/2021 at 00:16
 * using IntelliJ IDEA
 */
object Events {

    fun handleEvents(storage: AccessoryStorage, query: String?, session: Session): HttpResponse {
        if (query == null) throw Exception("Query missing for event!")
        session.shouldClose = true
        val split = query.split(":")
        val aid = split[0].toInt()
        val accessory = storage[aid]
        accessory.update()
        val sessionsToSendTo = mutableSetOf<Session>()
        val responses = split[1].split(",").map {
            val characteristic = accessory[it.toLong()]
            sessionsToSendTo.addAll(characteristic.subscribedSessions)
            CharacteristicResponse(aid, characteristic.iid, characteristic.value)
        }
        val httpResponse = HttpResponse(type = ResponseType.Event, data = appleGson.toJson(CharacteristicsResponse(responses)).toByteArray())

        sessionsToSendTo.forEach { it.sendMessage(httpResponse) }
        return HttpResponse(200)
    }


}