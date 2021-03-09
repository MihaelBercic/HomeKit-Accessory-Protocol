package homekit.structure.storage

import homekit.structure.data.Pairing
import utils.gson
import java.io.File

/**
 * Created by Mihael Valentin Berčič
 * on 14/02/2021 at 12:46
 * using IntelliJ IDEA
 *
 * This class serves as a storage for all current pairings with the bridge.
 */
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

