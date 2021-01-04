package homekit.tlv

import homekit.tlv.structure.Item
import homekit.tlv.structure.PairingMethod
import homekit.tlv.structure.TLVValue
import java.nio.ByteBuffer

/**
 * Created by Mihael Valentin Berčič
 * on 03/01/2021 at 00:36
 * using IntelliJ IDEA
 */

fun parseTLV(byteArray: ByteArray): List<Item> {
    val items = mutableListOf<Item>()
    val byteBuffer = ByteBuffer.wrap(byteArray)
    var previous: Item? = null
    loop@ while (byteBuffer.hasRemaining()) {
        val type = TLVValue.valueOf(byteBuffer.get())
        val length = byteBuffer.get().toInt() and 255
        val dataArray = ByteArray(length)
        byteBuffer.get(dataArray)

        if (previous?.identifier == type) {
            println("Length before adding fragment: ${previous.dataLength}")
            previous.data.addAll(dataArray.toList())
            println("Added fragmented data! Total size: ${previous.dataLength}")
        } else {
            val item: Item = when (type) {
                TLVValue.State -> StateItem(dataArray[0])
                TLVValue.Method -> MethodItem(PairingMethod.valueOf(dataArray[0]))
                TLVValue.PublicKey -> PublicKeyItem(dataArray)
                TLVValue.Proof -> EvidenceItem(dataArray)
                TLVValue.EncryptedData -> EncryptedItem(dataArray)
                else -> {
                    println("T: $type ($length)")
                    continue@loop
                }
            }
            items.add(0, item)
            previous = item
        }
    }
    return items
}