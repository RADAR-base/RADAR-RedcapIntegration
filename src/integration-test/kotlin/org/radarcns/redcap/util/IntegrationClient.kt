package org.radarcns.redcap.util

import okhttp3.MediaType
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import org.radarcns.redcap.webapp.util.PathLabels
import java.io.IOException

object IntegrationClient {
    private const val WEBAPP_HOST = "http://localhost"
    private const val WEBAPP_PORT = "8080"

    @JvmStatic
    fun makeTriggerRequest(body: String, mediaType: String): Response? {
        val request = Request.Builder()
            .url(WEBAPP_HOST + ":" + WEBAPP_PORT + "/redcap/" + PathLabels.REDCAP_TRIGGER)
            .post(RequestBody.create(MediaType.parse(mediaType), body))
            .build()
        try {
            IntegrationUtils.httpClient.newCall(request).execute().use { response ->
                if (response.code() != 200) {
                    IntegrationUtils.LOGGER.info("Code: " + response.code() + ", Info: " + response.body()!!.string())
                }
                return response
            }
        } catch (exc: IOException) {
            exc.printStackTrace()
        }
        return null
    }
}