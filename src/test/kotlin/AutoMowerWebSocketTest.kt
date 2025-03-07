import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import plugins.husqvarna.communication.HusqvarnaSocketListener
import plugins.husqvarna.structure.AutoMowerData
import plugins.husqvarna.structure.HusqvarnaAuth
import utils.Logger
import utils.gson
import utils.httpClient
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

class AutoMowerWebSocketTest {

    private val socketListener = HusqvarnaSocketListener {
        Logger.info("Status update! $this")
    }

    @Throws
    private fun authenticationRetrieval(): HusqvarnaAuth? {
        val clientId = "e7ecff83-00b7-4686-9368-d141fc9c4d31"
        val clientSecret = "556e8f73-3b1c-471b-a19b-40f586ef76d8"
        val id = "18033c27-86bf-4204-8dd8-42e64b138f74"

        val formEncoded = "grant_type=client_credentials&client_id=${clientId}&client_secret=${clientSecret}"
        return try {
            val request = HttpRequest.newBuilder(URI("https://api.authentication.husqvarnagroup.dev/v1/oauth2/token"))
                .headers("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(formEncoded))
                .build()
            val response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            val details = gson.fromJson(response.body(), HusqvarnaAuth::class.java).apply {
                createdAt = System.currentTimeMillis()
            }
            details
        } catch (e: Exception) {
            throw e
        }
    }

    @Test
    fun testAuthentication() {
        assert(authenticationRetrieval() != null)
    }

    @Test
    fun openWebSocket() {
        val authentication = authenticationRetrieval() ?: throw Exception("Unable to retrieve auth.")
        val webSocket = HttpClient.newHttpClient().newWebSocketBuilder()
            .header("Authorization", "Bearer ${authentication.accessToken}")
            .buildAsync(URI("wss://ws.openapi.husqvarna.dev/v1"), socketListener)
            .join()
        Thread.sleep(60 * 60 * 1000)
        assert(webSocket != null)
    }

    @Test
    fun testStatusEventParse() {
        val dummy = """{
  "id": "256b2365-33a7-46fe-a9fb-e67e84f4ac01",
  "type": "status-event",
  "attributes": {
    "battery": {
      "batteryPercent": 77
    },
    "mower": {
      "mode": "MAIN_AREA",
      "activity": "MOWING",
      "state": "IN_OPERATION",
      "errorCode": 0,
      "errorCodeTimestamp": 0 // In local time for the mower
    },
    "planner": {
      "nextStartTimestamp": 0, // In local time for the mower
      "override": {
        "action": "FORCE_MOW"
      },
      "restrictedReason": "PARK_OVERRIDE"
    },
    "metadata": {
      "connected": true,
      "statusTimestamp": 0 // In UTC time
    }
  }
}"""
        val parsedStatus = utils.gson.fromJson(dummy, AutoMowerData::class.java)
        Logger.debug(parsedStatus)
        assert(parsedStatus.type != null)
    }

    companion object {
        @JvmStatic
        @BeforeAll
        fun setUp(): Unit {
            Logger.info("Test started")
        }
    }

}
