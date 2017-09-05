package org.radarcns.redcap;

import java.io.IOException;
/*import java.net.URL;
import java.util.concurrent.TimeUnit;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;*/
import org.junit.Test;
/*import org.radarcns.oauth.OAuth2AccessToken;
import org.radarcns.oauth.OAuth2Client;
import org.radarcns.redcap.managementportal.Project;
import org.radarcns.redcap.managementportal.Subject;*/

/*
 * Copyright 2017 King's College London and The Hyve
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
public class TestMp {

    @Test
    public void testToken() throws IOException {
        /*OAuth2Client clientToken = new OAuth2Client()
            .tokenEndpoint(new URL("http://34.250.170.242:9000/oauth/token"))
            .clientId("radar_redcap_integrator")
            .clientSecret("my-secrect_token")
            .addScope("read")
            .addScope("write");

        OAuth2AccessToken token = new OAuth2AccessToken();

        if (token.isExpired()) {
            token = clientToken.getAccessToken();
        }

        OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build();

        Request request = new Request.Builder()
            .url("http://34.250.170.242:9000/api/projects/1")
            .addHeader("Authorization", "Bearer ".concat(token.getAccessToken()))
            .get()
            .build();

        Response response = client.newCall(request).execute();

        Project project = Project.getObject(response);

        Subject subject = new Subject("53d8a54a", 1,
            new URL("https://kclplxredweb01.wis.kcl.ac.uk/redcap_v6.10.1/DataEntry/"
                + "index.php?pid=33&id=1&event_id=baseline_assessment_arm_1&page=radar_enrolment"),
            "admin@localhost",
            project);

        //admin@localhost

        request = new Request.Builder()
            .url("http://34.250.170.242:9000/api/subjects")
            .addHeader("Authorization", "Bearer ".concat(token.getAccessToken()))
            .put(RequestBody.create(MediaType.parse("application/json"), subject.getJsonString()))
            .build();

        response = client.newCall(request).execute();

        System.out.println(response.code());

        System.out.println(response.body().string());*/
    }
}
