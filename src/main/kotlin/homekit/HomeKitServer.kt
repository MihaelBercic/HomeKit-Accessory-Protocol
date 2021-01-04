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
        config.showJavalinBanner = false

        before { println(it.path()) }

        exception(Exception::class.java) { exception, _ -> exception.printStackTrace() }

        post("/pair-setup", pairSetup::handleRequest)

        start(3000)
    }

}


