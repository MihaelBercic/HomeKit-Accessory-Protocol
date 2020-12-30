package homekit

import homekit.pairing.PairSetup
import io.javalin.Javalin

/**
 * Created by Mihael Valentin Berčič
 * on 22/12/2020 at 23:38
 * using IntelliJ IDEA
 */
class HomeKitServer : Javalin() {

    private val pairSetup = PairSetup()

    init {

        before {
            println(it.path())
        }


        exception(Exception::class.java) { exception, context ->
            exception.printStackTrace()
        }

        post("/pair-setup", pairSetup::handleRequest)

        post("/pair-verify") { println("Data from VERIFY!") }
        post("/accessories") { println("Data from ACCESSORIES!") }
        post("/pairings") { println("Data from PAIRINGS!") }

        start(3000)
    }

}


