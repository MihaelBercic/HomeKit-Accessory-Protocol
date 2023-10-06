package utils

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Created by Mihael Valentin Berčič
 * on 17/02/2021 at 18:52
 * using IntelliJ IDEA
 */
private val Long.toChunkedTimeStamp get() = toString().drop(4).chunked(3).joinToString(" ")
private val timestamp: String get() = System.currentTimeMillis().toChunkedTimeStamp

object Logger {

    private var isLoggingEnabled = true
    private val timeFormat = DateTimeFormatter.ofPattern("dd. MM | HH:mm:ss.SSS")

    const val red = "\u001b[31m"
    const val blue = "\u001B[34;1m"
    const val cyan = "\u001b[36m"
    const val green = "\u001b[32m"
    const val black = "\u001b[30m"
    const val yellow = "\u001b[33m"
    const val magenta = "\u001b[35m"
    const val white = "\u001b[37m"
    const val reset = "\u001B[0m"

    /**
     * Prints the given message with the coloring and debug information provided.
     *
     * @param debugType
     * @param message
     * @param color Text color
     */
    private fun log(message: Any?, color: String = black) {
        if (!isLoggingEnabled) return
        val timestamp = LocalDateTime.now().format(timeFormat).padEnd(11)
        println("$color[$timestamp]$reset\t$message")
    }

    fun info(message: Any?) = log(message, green)
    fun debug(message: Any?) = log(message, blue)
    fun error(message: Any?) = log(message, red)
    fun trace(message: Any?) = log(message, yellow)

}