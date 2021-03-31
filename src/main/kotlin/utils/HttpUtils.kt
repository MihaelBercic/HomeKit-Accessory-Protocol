package homekit.communication

import utils.HttpMethod
import utils.ResponseType
import java.util.regex.Pattern

/**
 * Created by Mihael Valentin Berčič
 * on 06/02/2021 at 00:42
 * using IntelliJ IDEA
 */
data class HttpHeaders(val httpMethod: HttpMethod, val path: String, val query: String?, val contentLength: Int)
class HttpRequest(val headers: HttpHeaders, val content: ByteArray)

open class Response(open val data: ByteArray = ByteArray(0))

open class HttpResponse(code: Int = 200, contentType: String = "application/hap+json", type: ResponseType = ResponseType.Http, vararg data: Byte = ByteArray(0)) :
    Response("${type.data} $code\r\nContent-Type: $contentType\r\nContent-Length: ${data.size}\r\n\r\n".toByteArray() + data)


val httpInfoPattern: Pattern = Pattern.compile("(?<method>\\w+) (?<path>/[a-zA-Z-_]+)(?<query>\\?[a-zA-Z0-9=.,:&]+)*")
val contentLengthPattern: Pattern = Pattern.compile("Content-Length: (?<length>\\d+)")

object Colors {
    const val RESET = "\u001B[0m"

    const val HIGH_INTENSITY = "\u001B[1m"
    const val LOW_INTENSITY = "\u001B[2m"

    const val ITALIC = "\u001B[3m"
    const val UNDERLINE = "\u001B[4m"
    const val BLINK = "\u001B[5m"
    const val RAPID_BLINK = "\u001B[6m"
    const val REVERSE_VIDEO = "\u001B[7m"
    const val INVISIBLE_TEXT = "\u001B[8m"

    const val BLACK = "\u001B[30m"
    const val RED = "\u001B[31m"
    const val GREEN = "\u001B[32m"
    const val YELLOW = "\u001B[33m"
    const val BLUE = "\u001B[34m"
    const val MAGENTA = "\u001B[35m"
    const val CYAN = "\u001B[36m"
    const val WHITE = "\u001B[37m"

    const val BACKGROUND_BLACK = "\u001B[40m"
    const val BACKGROUND_RED = "\u001B[41m"
    const val BACKGROUND_GREEN = "\u001B[42m"
    const val BACKGROUND_YELLOW = "\u001B[43m"
    const val BACKGROUND_BLUE = "\u001B[44m"
    const val BACKGROUND_MAGENTA = "\u001B[45m"
    const val BACKGROUND_CYAN = "\u001B[46m"
    const val BACKGROUND_WHITE = "\u001B[47m"
}
