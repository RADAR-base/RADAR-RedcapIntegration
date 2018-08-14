package org.radarcns.redcap.managementportal;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.radarcns.exception.TokenException;
import org.radarcns.oauth.OAuth2Client;
import org.radarcns.redcap.config.ManagementPortalInfo;
import org.radarcns.redcap.config.Properties;
import org.radarcns.redcap.config.RedCapManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.time.Duration;
import java.util.List;
import java.util.UUID;

/*
 * Copyright 2017 King's College London
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

/**
 * Client to interact with the RADAR Management Portal.
 */
public class MpClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(MpClient.class);

    private final OkHttpClient httpClient;

    private final OAuth2Client oauthClient;

    @Inject
    public MpClient(OkHttpClient httpClient) {
        this.httpClient = httpClient;

        try {
            oauthClient = new OAuth2Client.Builder()
                    .credentials(Properties.getOauthClientId(), Properties.getOauthClientSecret())
                    .endpoint(Properties.getTokenEndPoint())
                    .httpClient(this.httpClient).build();
        } catch (MalformedURLException ex) {
            throw new IllegalStateException("Failed to construct MP Token endpoint URL", ex);
        }
    }

    private String getToken() throws IOException {
        try {
            return oauthClient.getValidToken(Duration.ofSeconds(30)).getAccessToken();
        } catch (TokenException ex) {
            throw new IOException(ex);
        }
    }

    public Project getProject(URL redcapUrl, Integer projectId)
                throws IOException {
        ManagementPortalInfo mpInfo = RedCapManager.getRelatedMpInfo(redcapUrl, projectId);

        Request request = getBuilder(Properties.getProjectEndPoint(mpInfo)).get().build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (response.isSuccessful()) {
                Project project = Project.getObject(response);
                validateProject(Properties.getProjectEndPoint(mpInfo),
                        project, redcapUrl, projectId);

                LOGGER.debug("Retrieve project {}", project.toString());

                return project;
            }

            throw new IllegalStateException("Error while retrieving project info from "
                    + Properties.getProjectEndPoint(mpInfo) + ". Response code: "
                    + response.code() + " Message: " + response.message());
        }
        catch (IOException ex) {
            throw new IllegalStateException("IO error while retrieving project info from "
                    + Properties.getProjectEndPoint(mpInfo) + ".", ex);
        }
    }

    private static void validateProject(URL mp, Project project,
                                        URL redcapUrl, Integer projectId) {
        String message = "Project " + project.getId() + " at "
                + mp.toString() + " seems to be not linked to the REDCap project " + projectId
                + " at " + redcapUrl + ". Please check values set for "
                + Project.EXTERNAL_PROJECT_URL_KEY + " and " + Project.EXTERNAL_PROJECT_ID_KEY
                + " for Project " + project.getId() + " at " + mp.toString();

        try {
            if (!project.getRedCapId().equals(projectId)
                    || project.getRedCapUrl().equals(redcapUrl)) {
                throw new IllegalArgumentException(message);
            }
        } catch (MalformedURLException exc) {
            throw new IllegalArgumentException(message.concat(" ").concat(exc.getMessage()));
        }
    }

    public Subject createSubject(URL redcapUrl, Project project, Integer recordId,
            String humanReadableId) {
        //TODO check how to generate UUID
        String radarSubjectId = UUID.randomUUID().toString();

        Subject subject;
        Request request;
        try {
            subject = new Subject(radarSubjectId, recordId,
                    RedCapManager.getRecordUrl(redcapUrl, project.getRedCapId(), recordId),
                    project, humanReadableId);

            request = getBuilder(Properties.getSubjectEndPoint())
                    .put(RequestBody.create(MediaType.parse(
                            javax.ws.rs.core.MediaType.APPLICATION_JSON), subject.getJsonString()))
                    .build();
        } catch (IOException exc) {
            throw new IllegalStateException("Subject cannot be created", exc);
        }

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IllegalStateException("Subject cannot be created. Response code: "
                    + response.code() + " Message: " + response.message() + " Info: "
                        + response.body().string());
            } else {
                LOGGER.debug("Successfully created subject: {}", subject.getJsonString());
                return subject;
            }
        } catch (IOException exc) {
            throw new IllegalStateException("Subject cannot be created", exc);
        }
    }

    public Subject getSubject(URL redcapUrl, Integer projectId, Integer recordId)
            throws IOException, URISyntaxException {
        ManagementPortalInfo mpInfo = RedCapManager.getRelatedMpInfo(redcapUrl, projectId);

        Request request = getBuilder(getSubjectUrl(Properties.getSubjectEndPoint(),
                mpInfo.getProjectName(), recordId)).get().build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (response.isSuccessful()) {
                List<Subject> subjects = Subject.getObjects(response);

                if(subjects.size() > 1) {
                    throw new IllegalStateException("More than 1 subject exists same "
                            + "externalId exist in the same Project");
                }

                return subjects.get(0);
            }
            LOGGER.info("Subject is not present");
            return null;
        }
        catch (IOException exc) {
            throw new IllegalStateException("Subject could not be retrieved", exc);
        }
    }

    private Request.Builder getBuilder(URL url) throws IOException {
        return new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + getToken());
    }

    private static URL getSubjectUrl(URL url, String projectName, Integer recordId)
            throws URISyntaxException, MalformedURLException {
        URI oldUri = url.toURI();

        String parameters = "projectName=".concat(projectName).concat("&externalId=").concat(
                recordId.toString());
        String newQuery = oldUri.getQuery();
        if (newQuery == null) {
            newQuery = parameters;
        } else {
            newQuery += parameters;
        }

        URI newUri = new URI(oldUri.getScheme(), oldUri.getAuthority(), oldUri.getPath(), newQuery,
                oldUri.getFragment());

        LOGGER.info("URI = " + newUri.toString());

        return newUri.toURL();
    }
}
