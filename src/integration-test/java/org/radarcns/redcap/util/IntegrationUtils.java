package org.radarcns.redcap.util;

import java.io.IOException;
import java.net.MalformedURLException;
import java.time.Duration;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.radarcns.exception.TokenException;
import org.radarcns.oauth.OAuth2Client;
import org.radarcns.redcap.config.Properties;
import org.radarcns.redcap.managementportal.MpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IntegrationUtils {

    public static final Logger LOGGER = LoggerFactory.getLogger(IntegrationUtils.class);
    public static final MpClient mpClient;
    public static final String REDCAP_URL = "http://redcap.com/redcap_v6.10.1/";
    public static final int REDCAP_PROJECT_ID = 33;
    public static final int REDCAP_RECORD_ID_1 = 1;
    public static final int REDCAP_RECORD_ID_2 = 2;
    public static final String WORK_PACKAGE = "INTEG_TEST";
    public static final String MP_PROJECT_LOCATION = "LONDON";
    public static final int MP_PROJECT_ID = 1;
    public static final String TRIGGER_BODY = "redcap_url=https%3A%2F%2Fredcap.com%2Fredcap_v6.10.1%2F&"
            + "project_url=http://redcap.com/redcap_v6.10.1/index.php?pid=33&"
            + "project_id=" + REDCAP_PROJECT_ID + "&"
            + "username=test&"
            + "record=" + REDCAP_RECORD_ID_2 + "&"
            + "redcap_event_name=enrolment_arm_1&"
            + "instrument=radar_enrolment&"
            + "radar_enrolment_complete=0";
    private static final OAuth2Client oauthClient;
    private static final String EXTERNAL_PROJECT_URL = "http://redcap.com/redcap_v6.10.1/ProjectSetup/index.php?pid=33";
    // Use public static vars to use the same instance across all the tests
    public static OkHttpClient httpClient = new OkHttpClient();

    static {
        try {
            oauthClient = new OAuth2Client.Builder()
                    .credentials(Properties.getOauthClientId(), Properties.getOauthClientSecret())
                    .endpoint(Properties.getTokenEndPoint())
                    .httpClient(httpClient).build();
            mpClient = new MpClient(httpClient);
            LOGGER.info("Config: {}", Properties.INSTANCE.getCONFIG());
        } catch (MalformedURLException ex) {
            throw new IllegalStateException("Failed to construct MP Token endpoint URL", ex);
        }
    }

    /**
     * Add required attributes to the project. These are verified when creating an integration with redcap
     *
     * @throws IOException if request was unsuccessful
     */
    public static void updateProjectAttributes() throws IOException, TokenException {
        String Dto = "{\n" +
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
                "   \"id\": " + MP_PROJECT_ID + "}";

        Request request = new Request.Builder()
                .url(Properties.getProjectEndPoint())
                .addHeader("Authorization", "Bearer " .concat(
                        oauthClient.getValidToken(Duration.ofSeconds(30)).getAccessToken())).put(RequestBody.create(
                        MediaType.parse(javax.ws.rs.core.MediaType.APPLICATION_JSON)
                        , Dto)).build();

        try (Response response = httpClient.newCall(request).execute()) {
            int code = response.code();
            ResponseBody body = response.body();
            if (code!=200 || body==null) {
                throw new IOException("The project attributes could not be updated.");
            }
            LOGGER.info(
                    "Project Attributes Updated with status {} and body {} : ", code, body.string()
            );
        }
    }
}
