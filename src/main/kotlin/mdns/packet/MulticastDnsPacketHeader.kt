package mdns.packet

data class MulticastDnsPacketHeader(
    val identification: Short = 0,
    val isResponse: Boolean,
    val opcode: Int = 0,
    val isAuthoritativeAnswer: Boolean = false,
    val isTruncated: Boolean = false,
    val isRecursionDesired: Boolean = false,
    val isRecursionAvailable: Boolean = false,
    val responseCode: Int = 0
)