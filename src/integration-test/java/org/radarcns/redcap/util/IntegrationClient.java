package org.radarcns.redcap.util;

import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.radarcns.redcap.webapp.PathLabels;

import java.io.IOException;

import static org.radarcns.redcap.util.IntegrationUtils.LOGGER;
import static org.radarcns.redcap.util.IntegrationUtils.httpClient;

public class IntegrationClient {

    private static final String WEBAPP_HOST = "http://localhost";
    private static final String WEBAPP_PORT = "8080";

    public static Response makeTriggerRequest(String body, String mediaType) {
        Request request = new Request.Builder().url(WEBAPP_HOST + ":" + WEBAPP_PORT + "/redcap/" + PathLabels.REDCAP_TRIGGER)
                .post(RequestBody.create(MediaType.parse(mediaType), body))
                .build();

        try(Response response = httpClient.newCall(request).execute()) {
            if(response.code() != 200) {
                LOGGER.info("Code: " + response.code() + ", Info: " + response.body().string());
            }

            return response;
        } catch (IOException exc) {
            exc.printStackTrace();
        }

        return null;
    }
}
