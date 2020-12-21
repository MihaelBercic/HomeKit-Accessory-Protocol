package mdns.v2.records.structure

/**
 * Created by Mihael Valentin Berčič
 * on 14/12/2020 at 11:32
 * using IntelliJ IDEA
 */

enum class ResourceClass(val id: Int) {
    IN(1),
    NO(-1);

    companion object {
        fun withId(id: Int) = values().firstOrNull { it.id == id } ?: NO
    }
}


enum class RecordType(val id: Short) {
    Unsupported(-1),
    A(1),
    NS(2),
    CNAME(5),
    SOA(6),
    PTR(12),
    HINFO(13),
    MX(15),
    TXT(16),
    RP(17),
    AFSDB(18),
    SIG(24),
    KEY(25),
    AAAA(28),
    LOC(29),
    SRV(33),
    NAPTR(35),
    KX(36),
    CERT(37),
    DNAME(39),
    APL(42),
    DS(43);

    companion object {
        fun typeOf(id: Short) = values().firstOrNull { it.id == id } ?: Unsupported
    }
}