package homekit.pairing

import homekit.tlv.StateItem
import homekit.tlv.structure.Packet
import io.javalin.http.Context

/**
 * Created by Mihael Valentin Berčič
 * on 28/12/2020 at 13:38
 * using IntelliJ IDEA
 */
class PairSetup {

    var currentState: Int = 1

    fun handleRequest(context: Context) {
        val packet = Packet(context.bodyAsBytes())
        val stateItem = packet.find { it is StateItem } as? StateItem ?: return

        val requestedValue = stateItem.value.toInt()
        if (requestedValue != currentState) return

        when (requestedValue) {
            1 -> ""
            2 -> ""
            else -> ""
        }

    }

}