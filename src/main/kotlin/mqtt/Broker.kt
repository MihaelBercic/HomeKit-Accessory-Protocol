package mqtt

import java.net.ServerSocket

/**
 * Created by Mihael Valentin Berčič
 * on 20/03/2021 at 15:55
 * using IntelliJ IDEA
 */
class Broker {

    var isListening = true

    fun startListening(port: Int) {
        ServerSocket(port).apply {
            soTimeout = 0
            while (isListening) {
                val socket = accept()
                Thread { MqttSession(socket) }.start()
            }
        }
    }

}