package homekit.structure.data

import utils.gson
import java.io.File

class PairingStorage(val list: MutableList<Pairing> = mutableListOf()) {

    val isPaired: Boolean get() = list.isNotEmpty()

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