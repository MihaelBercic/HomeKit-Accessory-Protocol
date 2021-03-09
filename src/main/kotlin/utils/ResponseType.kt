package utils

enum class ResponseType(val data: String) {
    Http("HTTP/1.1"),
    Event("EVENT/1.0")
}