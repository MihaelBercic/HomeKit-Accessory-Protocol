package plugins.husqvarna.communication

import plugins.husqvarna.structure.AutoMowerData
import utils.Logger
import utils.gson
import java.lang.Exception
import java.net.http.WebSocket
import java.util.Timer
import java.util.TimerTask
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletionStage
import kotlin.concurrent.scheduleAtFixedRate

open class HusqvarnaSocketListener(private val onStatusUpdate: AutoMowerData.() -> Unit) : WebSocket.Listener {

    private var timer: TimerTask? = null

    override fun onOpen(webSocket: WebSocket) {
        super.onOpen(webSocket)
        timer = Timer().scheduleAtFixedRate(0, 60_000) {
            webSocket.sendText("ping", false)
            Logger.info("Sent ping!")
        }
        Logger.info("Opened socket to $webSocket")
    }

    override fun onText(webSocket: WebSocket?, data: CharSequence?, last: Boolean): CompletionStage<*> {
        Logger.info("Received $data attempting to parse status...")
        val status = gson.fromJson<AutoMowerData?>(data.toString(), AutoMowerData::class.java)
        Logger.debug("Status parsed = $status (${status == null})")
        if (status?.type != null) onStatusUpdate(status)
        Logger.debug("Waiting for further data.")
        return CompletableFuture.completedStage(true)
    }

    override fun onClose(webSocket: WebSocket?, statusCode: Int, reason: String?): CompletionStage<*> {
        Logger.info("Closed the socket $statusCode --- $reason")
        return super.onClose(webSocket, statusCode, reason)
    }

    override fun onError(webSocket: WebSocket?, error: Throwable?) {
        super.onError(webSocket, error)
        Logger.error("HusqvarnaSocket error $error")
    }

}