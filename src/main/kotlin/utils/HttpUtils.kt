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

class HttpResponse(code: Int = 200, contentType: String = "application/hap+json", type: ResponseType = ResponseType.Http, vararg data: Byte = ByteArray(0)) :
    Response("${type.data} $code\r\nContent-Type: $contentType\r\nContent-Length: ${data.size}\r\n\r\n".toByteArray() + data)


val httpInfoPattern: Pattern = Pattern.compile("(?<method>\\w+) (?<path>/[a-zA-Z-_]+)(?<query>\\?[a-zA-Z0-9=.,:&]+)*")
val contentLengthPattern: Pattern = Pattern.compile("Content-Length: (?<length>\\d+)")

