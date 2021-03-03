package homekit.communication.structure.data

import com.google.gson.annotations.Expose
import homekit.structure.Accessory
import utils.gson
import java.io.File

/**
 * Created by Mihael Valentin Berčič
 * on 14/02/2021 at 13:15
 * using IntelliJ IDEA
 */
class AccessoryStorage(bridge: Accessory) {

    @Expose
    private val accessories: MutableList<Accessory> = mutableListOf()
    private val accessoryMap: HashMap<Int, Accessory> = hashMapOf()

    operator fun get(aid: Int) = accessoryMap[aid] ?: throw Exception("No accessory with aid of $aid.")
    fun contains(aid: Int) = accessoryMap.contains(aid)

    fun addAccessory(accessory: Accessory) {
        accessories.add(accessory)
        accessoryMap[accessory.aid] = accessory
    }

    init {
        addAccessory(bridge)
    }
}

class Pairing(@Expose val identifier: String, @Expose val publicKey: ByteArray, @Expose var isAdmin: Boolean)
class PairingStorage(@Expose val list: MutableList<Pairing> = mutableListOf()) {

    fun addPairing(pairing: Pairing) {
        list.add(pairing)
        save()
    }

    fun findPairing(identifier: String) = list.firstOrNull { it.identifier == identifier }

    fun removePairing(identifier: String) {
        list.removeIf { it.identifier == identifier }
        save()
    }

    private fun save() = File("pairings.json").writeText(gson.toJson(this))

}

