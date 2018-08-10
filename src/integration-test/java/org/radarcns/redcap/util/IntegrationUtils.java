package org.radarcns.redcap.util;

import okhttp3.*;
import org.radarcns.exception.TokenException;
import org.radarcns.redcap.config.Properties;
import org.radarcns.redcap.listener.TokenManagerListener;

import javax.servlet.ServletContext;
import java.io.IOException;
import java.util.logging.Logger;

public class IntegrationUtils {

    private static final Logger LOGGER = Logger.getLogger(IntegrationUtils.class.getName());

    public static final String HTTP_CLIENT = "HTTP_CLIENT";
    public static final String REDCAP_URL = "http://redcap.com/redcap_v6.10.1/";
    public static final int REDCAP_PROJECT_ID = 33;
    public static final int REDCAP_RECORD_ID = 1;

    public static final String WORK_PACKAGE = "INTEG_TEST";
    private static final String EXTERNAL_PROJECT_URL = "http://redcap.com/redcap_v6.10.1/ProjectSetup/index.php?pid=33";

    /**
     * Add required attributes to the project. These are verified when creating an integration with redcap
     * @throws IOException if request was unsuccessful
     */
    public static void updateProjectAttributes(OkHttpClient httpClient, ServletContext context) throws IOException, TokenException {
        String Dto = "{\n" +
                "  \"attributes\": \n" +
                "    {\n" +
                "      \"Work-package\": \""+ WORK_PACKAGE +"\",\n" +
                "      \"External-project-id\": \""+ REDCAP_PROJECT_ID +"\",\n" +
                "      \"External-project-url\": \""+ EXTERNAL_PROJECT_URL +"\"\n" +
                "    }\n" +
                "  ,\n" +
                "  \"projectName\": \"radar\",\n" +
                "   \"location\": \"London\",\n" +
                "   \"description\": \"RadarTest\",\n" +
                "   \"id\": 1}";

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
