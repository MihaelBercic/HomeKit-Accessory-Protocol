package homekit.structure.storage

import com.google.gson.annotations.Expose
import homekit.communication.HttpResponse
import homekit.structure.Accessory
import utils.appleGson

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

    fun createHttpResponse() = HttpResponse(data = appleGson.toJson(this).toByteArray())
}