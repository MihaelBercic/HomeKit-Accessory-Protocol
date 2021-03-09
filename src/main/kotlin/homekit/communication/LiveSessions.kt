package homekit.communication

import homekit.structure.Characteristic
import utils.Logger

/**
 * Created by Mihael Valentin Berčič
 * on 03/03/2021 at 18:09
 * using IntelliJ IDEA
 */
object LiveSessions {

    private val currentSession: Session get() = threadSessionMap[Thread.currentThread()] ?: throw Exception("Current thread has no session attached!")

    private val threadSessionMap: MutableMap<Thread, Session> = mutableMapOf()
    private val sessionSubscriptions: MutableMap<Session, MutableList<Characteristic>> = mutableMapOf()
    private val registeredCharacteristics: MutableMap<Characteristic, MutableList<Session>> = mutableMapOf()

    internal val Characteristic.isCurrentSessionSubscribed get() = registeredCharacteristics[this]?.contains(currentSession) ?: false
    internal val Characteristic.subscribedSessions get() = registeredCharacteristics[this] ?: emptyList()

    fun Session.secureSessionStarted() {
        threadSessionMap[Thread.currentThread()] = this
    }

    fun Session.subscribeFor(characteristic: Characteristic) {
        sessionSubscriptions.computeIfAbsent(this) { mutableListOf() }.add(characteristic)
        registeredCharacteristics.computeIfAbsent(characteristic) { mutableListOf() }.add(this)
        Logger.info("Subscribed ${currentController.identifier} for the ${characteristic.type}[${characteristic.iid shr 16}]")
    }

    fun Session.unsubscribeFrom(characteristic: Characteristic) {
        sessionSubscriptions[this]?.remove(characteristic)
        registeredCharacteristics[characteristic]?.remove(this)
        Logger.info("Unsubscribed ${currentController.identifier} from the ${characteristic.type}[${characteristic.iid shr 16}]")
    }

    fun Session.removeFromLiveSessions() {
        threadSessionMap.remove(Thread.currentThread())
        sessionSubscriptions.remove(this)?.forEach { unsubscribeFrom(it) }
    }

}