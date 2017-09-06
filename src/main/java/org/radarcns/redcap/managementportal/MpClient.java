package org.radarcns.redcap.managementportal;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Objects;
import java.util.UUID;
import javax.servlet.ServletContext;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.radarcns.redcap.config.ManagementPortalInfo;
import org.radarcns.redcap.config.Properties;
import org.radarcns.redcap.config.RedCapManager;
import org.radarcns.redcap.listener.HttpClientListener;
import org.radarcns.redcap.listener.TokenManagerListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    private static final String SEPARATOR = "-";

    private String radarSubjectId;
    private String humanReadableId;

    private String radarProjectId;

    /**
     * <p>Constructor. Starting from the given input it<ul>
     *     <li>retrieves the Management Portal project associated to the given REDCap instance
     *          and project identifier</li>
     *     <li>creates the Human Readable Identifier that is going to be associated with the
     *          created subject</li>
     *     <li>makes an HTTP PUT request on the Management Portal to create the subject if
     *          needed</li>
     *     <li>adds REDCap Record identifier, Human Readable Identifier and REDCap project URL to
     *          the subject stored in the Management Portal</li>
     *     <li>updates the REDCap form involved in the integration</li>
     * </ul></p>
     * <p>Note that REDCap will trigger upon any updates. For this reason this function may
     * be executed multiple times even if the two components have been already linked.</p>
     * @param redcapUrl {@link URL} pointing the REDCap instance that has issued the trigger
     * @param projectId {@link Integer} representing REDCap project identifier within the trigger
     *      has been generated
     * @param recordId {@link Integer} representing REDCap record identifier involved in the
     *      creation
     * @param context {@link ServletContext} useful to retrieve shared {@link OkHttpClient} and
     *      {@code access token}
     * @throws IllegalStateException in case the object cannot be created
     * @see org.radarcns.redcap.listener.HttpClientListener
     * @see org.radarcns.redcap.listener.TokenManagerListener
     */
    public MpClient(URL redcapUrl, Integer projectId,
            Integer recordId, ServletContext context) {
        Objects.requireNonNull(redcapUrl);
        Objects.requireNonNull(projectId);
        Objects.requireNonNull(recordId);
        Objects.requireNonNull(context);

        try {
            Project project = getProject(redcapUrl, projectId, context);

            String radarWorkPackage = project.getWorkPackage().toUpperCase();
            String location = project.getLocation().toUpperCase();

            humanReadableId = radarWorkPackage.concat(SEPARATOR).concat(
                    radarProjectId).concat(SEPARATOR).concat(location).concat(
                    SEPARATOR).concat(recordId.toString());

            Subject subject = getSubject(redcapUrl, projectId, recordId, context);

            if (Objects.isNull(subject)) {
                createSubject(redcapUrl, project, recordId, humanReadableId, context);

                LOGGER.info("Created RADAR subject: {}. Human readable identifier is: {}",
                    radarSubjectId, humanReadableId);
            } else {
                LOGGER.info("Subject for Record Id: {} at {} is already available.", recordId,
                        redcapUrl);
                //TODO check that Human Readable Identifier is correct. If not update Subject.
            }
        } catch (Exception exc) {
            LOGGER.error(exc.getMessage(), exc);
            throw new IllegalStateException("Subject creation cannot be completed.", exc);
        }
    }

    public String getRadarSubjectId() {
        return radarSubjectId;
    }

    public String getHumanReadableId() {
        return humanReadableId;
    }

    private Project getProject(URL redcapUrl, Integer projectId,
            ServletContext context) throws IOException {
        ManagementPortalInfo mpInfo = RedCapManager.getRelatedMpInfo(redcapUrl, projectId);

        Request request = getBuilder(Properties.getProjectEndPoint(mpInfo), context).get().build();

        Response response = HttpClientListener.getClient(context).newCall(request).execute();

        if (response.isSuccessful()) {
            Project project = Project.getObject(response);
            validateProject(Properties.getProjectEndPoint(mpInfo), project, redcapUrl, projectId);

            radarProjectId = project.getId().toString();

            LOGGER.debug("Retrieve project {}", project.toString());

            return project;
        }

        throw new IllegalStateException("Error while retrieving project info from "
                + Properties.getProjectEndPoint(mpInfo) + ". Response code: " + response.code()
                + " Message: " + response.message());
    }

    private static void validateProject(URL mp, Project project, URL redcapUrl, Integer projectId) {
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

    private void createSubject(URL redcapUrl, Project project, Integer recordId,
            String humanReadableId, ServletContext context) {
        try {
            //TODO check how to generate UUID
            radarSubjectId = UUID.randomUUID().toString();

            Subject subject = new Subject(radarSubjectId, recordId,
                    RedCapManager.getRecordUrl(redcapUrl, project.getRedCapId(), recordId),
                    project, humanReadableId);

            Request request = getBuilder(Properties.getSubjectEndPoint(), context)
                        .put(RequestBody.create(MediaType.parse(
                                javax.ws.rs.core.MediaType.APPLICATION_JSON),
                                subject.getJsonString()))
                        .build();

            Response response = HttpClientListener.getClient(context).newCall(request).execute();
            response.close();

            if (!response.isSuccessful()) {
                throw new IllegalStateException("Subject cannot be created. Response code: "
                    + response.code() + " Message: " + response.message());
            } else {
                LOGGER.debug("Successfully created subject: {}", subject.getJsonString());
            }
        } catch (MalformedURLException exc) {
            throw new IllegalStateException("Subject cannot be created".concat(
                    " ").concat(exc.getMessage()));
        } catch (IOException exc) {
            throw new IllegalStateException("Subject cannot be created".concat(
                    " ").concat(exc.getMessage()));
        }
    }

    private Subject getSubject(URL redcapUrl, Integer projectId, Integer recordId,
            ServletContext context) throws IOException, URISyntaxException {
        ManagementPortalInfo mpInfo = RedCapManager.getRelatedMpInfo(redcapUrl, projectId);

        Request request = getBuilder(getSubjectUrl(Properties.getSubjectEndPoint(),
                mpInfo.getProjectId(), recordId), context).get().build();

        Response response = HttpClientListener.getClient(context).newCall(request).execute();

        if (response.isSuccessful()) {
            Subject subject = Subject.getObject(response);

            radarSubjectId = subject.getSubjectId();

            return subject;
        }

        response.close();

        LOGGER.info("Subject is not present");
        return null;
    }

    private static Request.Builder getBuilder(URL url, ServletContext context) {
        return new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer ".concat(
                        TokenManagerListener.getToken(context)));
    }

    private static URL getSubjectUrl(URL url, Integer projectId, Integer recordId)
            throws URISyntaxException, MalformedURLException {
        URI oldUri = url.toURI();

        String parameters = "projectId=".concat(projectId.toString()).concat("&externalId=").concat(
                recordId.toString());
        String newQuery = oldUri.getQuery();
        if (newQuery == null) {
            newQuery = parameters;
        } else {
            newQuery += parameters;
        }

        URI newUri = new URI(oldUri.getScheme(), oldUri.getAuthority(), oldUri.getPath(), newQuery,
                oldUri.getFragment());

        LOGGER.info(newUri.toString());

        return newUri.toURL();
    }
}
