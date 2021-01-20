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
        config.showJavalinBanner = true
        before { println("Path: \u001B[31m ${it.path()}\u001B[0m") }

        exception(Exception::class.java) { exception, context ->
            exception.printStackTrace()
            context.status(404)
        }

        post("/pair-setup", pairSetup::handleRequest)

        start(3000)
    }

}


