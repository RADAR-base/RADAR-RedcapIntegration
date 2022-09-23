package org.radarbase.redcap.util

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import org.radarbase.exception.TokenException
import org.radarbase.oauth.OAuth2Client
import org.radarbase.redcap.config.Properties.CONFIG
import org.radarbase.redcap.config.Properties.oauthClientId
import org.radarbase.redcap.config.Properties.oauthClientSecret
import org.radarbase.redcap.config.Properties.projectEndPoint
import org.radarbase.redcap.config.Properties.tokenEndPoint
import org.radarbase.redcap.managementportal.MpClient
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.IOException
import java.net.MalformedURLException
import java.time.Duration
import javax.ws.rs.core.MediaType

object IntegrationUtils {
    val LOGGER: Logger = LoggerFactory.getLogger(IntegrationUtils::class.java)

    val httpClient = OkHttpClient()

    @JvmField
    val mpClient: MpClient =
        MpClient(httpClient)

    const val REDCAP_URL = "http://redcap.com/redcap_v6.10.1/"
    const val REDCAP_PROJECT_ID = 33
    const val REDCAP_RECORD_ID_1 = 1
    const val REDCAP_RECORD_ID_2 = 2
    const val WORK_PACKAGE = "INTEG_TEST"
    const val MP_PROJECT_LOCATION = "LONDON"
    const val MP_PROJECT_ID = 1
    const val TRIGGER_BODY = ("redcap_url=http://redcap.com/redcap_v6.10.1/&"
            + "project_url=http://redcap.com/redcap_v6.10.1/index.php?pid=33&"
            + "project_id=" + REDCAP_PROJECT_ID + "&"
            + "username=test&"
            + "record=" + REDCAP_RECORD_ID_2 + "&"
            + "redcap_event_name=enrolment_arm_1&"
            + "instrument=radar_enrolment&"
            + "radar_enrolment_complete=0")

    private val oauthClient: OAuth2Client by lazy {
        try {
            LOGGER.info("Config: {}", CONFIG)
            OAuth2Client.Builder()
                .credentials(
                    oauthClientId,
                    oauthClientSecret
                )
                .endpoint(tokenEndPoint)
                .httpClient(httpClient).build()
        } catch (ex: MalformedURLException) {
            throw IllegalStateException("Failed to construct MP Token endpoint URL", ex)
        }
    }
    private const val EXTERNAL_PROJECT_URL =
        "http://redcap.com/redcap_v6.10.1/ProjectSetup/index.php?pid=33"


    /**
     * Add required attributes to the project. These are verified when creating an integration with redcap
     *
     * @throws IOException if request was unsuccessful
     */
    @Throws(IOException::class, TokenException::class)
    fun updateProjectAttributes() {
        val dto = "{\n" +
                "  \"attributes\": \n" +
                "    {\n" +
                "      \"Work-package\": \"" + WORK_PACKAGE + "\",\n" +
                "      \"External-project-id\": \"" + REDCAP_PROJECT_ID + "\",\n" +
                "      \"External-project-url\": \"" + EXTERNAL_PROJECT_URL + "\"\n" +
                "    }\n" +
                "  ,\n" +
                "  \"projectName\": \"radar\",\n" +
                "   \"location\": \"" + MP_PROJECT_LOCATION + "\",\n" +
                "   \"description\": \"RadarTest\",\n" +
                "   \"id\": " + MP_PROJECT_ID + "}"

        val request = Request.Builder()
            .url(projectEndPoint)
            .addHeader(
                "Authorization", "Bearer " +
                        oauthClient.getValidToken(Duration.ofSeconds(30)).accessToken
            ).put(
                RequestBody.create(okhttp3.MediaType.parse(MediaType.APPLICATION_JSON), dto)
            ).build()

        httpClient.newCall(request).execute().use { response ->
            val code = response.code()
            val body = response.body()
            if (code != 200 || body == null) {
                throw IOException("The project attributes could not be updated.")
            }
            LOGGER.info(
                "Project Attributes Updated with status {} and body {} : ", code, body.string()
            )
        }
    }
}