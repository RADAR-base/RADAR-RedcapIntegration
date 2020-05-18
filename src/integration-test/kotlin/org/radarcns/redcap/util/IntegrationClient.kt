package org.radarcns.redcap.util

import okhttp3.*
import org.radarcns.redcap.webapp.util.PathLabels
import java.io.IOException

object IntegrationClient {
    private const val WEBAPP_HOST = "http://localhost"
    private const val WEBAPP_PORT = "8080"

    fun makeTriggerRequest(body: String, mediaType: String): Response {
        val request = Request.Builder()
            .url(WEBAPP_HOST + ":" + WEBAPP_PORT + "/redcap/" + PathLabels.REDCAP_TRIGGER)
            .post(RequestBody.create(MediaType.parse(mediaType), body))
            .build()
        return try {
            IntegrationUtils.httpClient.newCall(request).execute().use { response ->
                if (response.code() != 200) {
                    IntegrationUtils.LOGGER.info(
                        "Code: " + response.code() + ", Info: " + response.body()!!.string()
                    )
                }
                response
            }
        } catch (exc: IOException) {
            IntegrationUtils.LOGGER.info("Error: {}", exc.localizedMessage, exc)
            Response.Builder()
                .protocol(Protocol.HTTP_1_1)
                .request(request)
                .code(500)
                .message(exc.localizedMessage)
                .body(
                    ResponseBody.create(MediaType.parse("text/plain"), exc.localizedMessage)
                ).build()
        }
    }
}