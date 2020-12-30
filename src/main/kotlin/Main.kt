import homekit.HomeKitServer
import homekit.HomeKitService

/**
 * Created by Mihael Valentin Berčič
 * on 08/12/2020 at 22:41
 * using IntelliJ IDEA
 */


fun main() {
    /*
    val secureRandom = SecureRandom()
val N = SRP6CryptoParams.N_3072
val g = SRP6CryptoParams.g_large
val digest = MessageDigest.getInstance("SHA-512")
val crypto = SRP6CryptoParams(N, g, "SHA-512")
val generator = SRP6VerifierGenerator(crypto)
val routines = SRP6Routines()

    val password = "password123"
    val username = "alice"
    val salt = bigIntFromString("BEB25379 D1A8581E B5A72767 3A2441EE").toByteArray()
    val x = routines.computeX(digest, salt, password.toByteArray())
    digest.reset()
    val verifier = BigInteger(
        bigIntFromString(
            "9B5E0617 01EA7AEB 39CF6E35 19655A85 3CF94C75 CAF2555E F1FAF759 BB79CB47 " +
                    "7014E04A 88D68FFC 05323891 D4C205B8 DE81C2F2 03D8FAD1 B24D2C10 9737F1BE " +
                    "BBD71F91 2447C4A0 3C26B9FA D8EDB3E7 80778E30 2529ED1E E138CCFC 36D4BA31 " +
                    "3CC48B14 EA8C22A0 186B222E 655F2DF5 603FD75D F76B3B08 FF895006 9ADD03A7 " +
                    "54EE4AE8 8587CCE1 BFDE3679 4DBAE459 2B7B904F 442B041C B17AEBAD 1E3AEBE3 " +
                    "CBE99DE6 5F4BB1FA 00B0E7AF 06863DB5 3B02254E C66E781E 3B62A821 2C86BEB0 " +
                    "D50B5BA6 D0B478D8 C4E9BBCE C2176532 6FBD1405 8D2BBDE2 C33045F0 3873E539 " +
                    "48D78B79 4F0790E4 8C36AED6 E880F557 427B2FC0 6DB5E1E2 E1D7E661 AC482D18 " +
                    "E528D729 5EF74372 95FF1A72 D4027717 13F16876 DD050AE5 B7AD53CC B90855C9 " +
                    "39566483 58ADFD96 6422F524 98732D68 D1D7FBEF 10D78034 AB8DCB6F 0FCF885C " +
                    "C2B2EA2C 3E6AC866 09EA058A 9DA8CC63 531DC915 414DF568 B09482DD AC1954DE " +
                    "C7EB714F 6FF7D44C D5B86F6B D1158109 30637C01 D0F6013B C9740FA2 C633BA89"
        ).toByteArray()
    )
    val k = routines.computeK(digest, N, g)
    digest.reset()

    val privateValue = BigInteger(bigIntFromString("E487CB59 D31AC550 471E81F0 0F6928E0 1DDA08E9 74A004F4 9E61F5D1 05284D20").toByteArray().reversedArray())

    println(privateValue.toByteArray().asHexString)
    return
    val publicValue = routines.computePublicServerValue(N, g, k, verifier, privateValue)

    val u = bigIntFromString("03AE5F3C 3FA9EFF1 A50D7DBB 8D2F60A1 EA66EA71 2D50AE97 6EE34641 A1CD0E51 C4683DA3 83E8595D 6CB56A15 D5FBC754 3E07FBDD D316217E 01A391A1 8EF06DFF")
    val aPrivate = BigInteger(bigIntFromString("60975527 035CF2AD 1989806F 0407210B C81EDC04 E2762A56 AFD529DD DA2D4393").toByteArray())
    val aPublic = BigInteger(
        bigIntFromString(
            "FAB6F5D2 615D1E32 3512E799 1CC37443 F487DA60 4CA8C923 0FCB04E5 41DCE628 " +
                    "0B27CA46 80B0374F 179DC3BD C7553FE6 2459798C 701AD864 A91390A2 8C93B644 " +
                    "ADBF9C00 745B942B 79F9012A 21B9B787 82319D83 A1F83628 66FBD6F4 6BFC0DDB " +
                    "2E1AB6E4 B45A9906 B82E37F0 5D6F97F6 A3EB6E18 2079759C 4F684783 7B62321A " +
                    "C1B4FA68 641FCB4B B98DD697 A0C73641 385F4BAB 25B79358 4CC39FC8 D48D4BD8 " +
                    "67A9A3C1 0F8EA121 70268E34 FE3BBE6F F89998D6 0DA2F3E4 283CBEC1 393D52AF " +
                    "724A5723 0C604E9F BCE583D7 613E6BFF D67596AD 121A8707 EEC46944 95703368 " +
                    "6A155F64 4D5C5863 B48F61BD BF19A53E AB6DAD0A 186B8C15 2E5F5D8C AD4B0EF8 " +
                    "AA4EA500 8834C3CD 342E5E0F 167AD045 92CD8BD2 79639398 EF9E114D FAAAB919 " +
                    "E14E8509 89224DDD 98576D79 385D2210 902E9F9B 1F2D86CF A47EE244 635465F7 " +
                    "1058421A 0184BE51 DD10CC9D 079E6F16 04E7AA9B 7CF7883C 7D4CE12B 06EBE160 " +
                    "81E23F27 A231D184 32D7D1BB 55C28AE2 1FFCF005 F57528D1 5A88881B B3BBB7FE"
        ).toByteArray()
    )

    routines.computeSessionKey(N, g, BigInteger(u.toByteArray()), aPublic, privateValue)



    return
    */
    HomeKitService().startAdvertising(30000)
    HomeKitServer()
}

fun bigIntFromString(string: String): List<Byte> {
    return string.split(" ")
        .map { it.chunked(2).map { Integer.valueOf(it, 16) } }
        .flatten()
        .map { it.toByteArray() }
        .flatMap { it.toList() }
}

fun Int.toByteArray() = ByteArray(4).apply {
    set(3, (this@toByteArray and 0xFF).toByte())
    set(2, ((this@toByteArray ushr 8) and 0xFF).toByte())
    set(1, ((this@toByteArray ushr 16) and 0xFF).toByte())
    set(0, ((this@toByteArray ushr 24) and 0xFF).toByte())
}