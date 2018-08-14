package org.radarcns.redcap.util;

import okhttp3.*;
import org.mockito.Mockito;
import org.radarcns.exception.TokenException;
import org.radarcns.redcap.config.Properties;
import org.radarcns.redcap.listener.TokenManagerListener;
import org.radarcns.redcap.managementportal.MpClient;

import javax.servlet.ServletContext;
import java.io.IOException;
import java.util.logging.Logger;

public class IntegrationUtils {

    public static final Logger LOGGER = Logger.getLogger(IntegrationUtils.class.getName());

    // Use public static vars to use the same instance across all the tests
    public static final OkHttpClient httpClient = new OkHttpClient();
    public static ServletContext context = Mockito.mock(ServletContext.class);
    public static MpClient mpClient;
    public static final String HTTP_CLIENT = "HTTP_CLIENT";


    public static final String REDCAP_URL = "http://redcap.com/redcap_v6.10.1/";
    public static final int REDCAP_PROJECT_ID = 33;
    public static final int REDCAP_RECORD_ID_1 = 1;
    public static final int REDCAP_RECORD_ID_2 = 2;

    public static final String WORK_PACKAGE = "INTEG_TEST";
    private static final String EXTERNAL_PROJECT_URL = "http://redcap.com/redcap_v6.10.1/ProjectSetup/index.php?pid=33";
    public static final String MP_PROJECT_LOCATION = "LONDON";
    public static final int MP_PROJECT_ID = 1;

    public static final String TRIGGER_BODY = "redcap_url=https%3A%2F%2Fredcap.com%2Fredcap_v6.10.1%2F&"
            + "project_url=http://redcap.com/redcap_v6.10.1/index.php?pid=33&"
            + "project_id="+ REDCAP_PROJECT_ID +"&"
            + "username=test&"
            + "record="+ REDCAP_RECORD_ID_2 +"&"
            + "redcap_event_name=enrolment_arm_1&"
            + "instrument=radar_enrolment&"
            + "radar_enrolment_complete=0" ;


    static {
        context.setAttribute(HTTP_CLIENT, httpClient);
        mpClient = new MpClient(httpClient);
    }

    /**
     * Add required attributes to the project. These are verified when creating an integration with redcap
     * @throws IOException if request was unsuccessful
     */
    public static void updateProjectAttributes() throws IOException, TokenException {
        String Dto = "{\n" +
                "  \"attributes\": \n" +
                "    {\n" +
                "      \"Work-package\": \""+ WORK_PACKAGE +"\",\n" +
                "      \"External-project-id\": \""+ REDCAP_PROJECT_ID +"\",\n" +
                "      \"External-project-url\": \""+ EXTERNAL_PROJECT_URL +"\"\n" +
                "    }\n" +
                "  ,\n" +
                "  \"projectName\": \"radar\",\n" +
                "   \"location\": \""+ MP_PROJECT_LOCATION +"\",\n" +
                "   \"description\": \"RadarTest\",\n" +
                "   \"id\": "+ MP_PROJECT_ID +"}";

        Request request = new Request.Builder()
                .url(Properties.getProjectEndPoint())
                .addHeader("Authorization", "Bearer ".concat(
                        TokenManagerListener.getToken(context))).put(RequestBody.create(
                        MediaType.parse(javax.ws.rs.core.MediaType.APPLICATION_JSON)
                        , Dto)).build();

        try (Response response = httpClient.newCall(request).execute()) {
            LOGGER.info("Project Attributes Updated with status {} " + response.code() + "and body {} : " + response.body().string());
        }
    }
}
