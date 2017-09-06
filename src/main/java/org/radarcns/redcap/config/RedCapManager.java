package org.radarcns.redcap.config;

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

import java.net.MalformedURLException;
import java.net.URL;
import org.radarcns.redcap.util.RedCapTrigger;

/**
 * Information about the REDCap user used to query the REDCap API.
 */
public final class RedCapManager {

    private static final String PROJECT_ID = "DataEntry/index.php?pid=";
    private static final String RECORD_ID = "&id=";
    private static final String EVENT_ID = "&event_id=";
    private static final String PAGE_NAME = "&page=";

    private RedCapManager() {
        //Static class
    }

    /**
     * Given a {@link RedCapTrigger}, the function checks whether there is a valid configuration
     *      for the couple REDCap URL instance and REDCap project identifier specified in the
     *      trigger. If the configuration is available, it returns {@code true}. it returns
     *      {@code false} otherwise.
     * @param trigger {@link RedCapTrigger} that has hit the service
     * @return {@code true} if a valid configuration is available to handle this trigger,
     *      {@code false} otherwise
     */
    public static boolean isSupportedInstance(RedCapTrigger trigger) {
        return Properties.isSupportedInstance(trigger.getRedcapUrl(), trigger.getProjectId());
    }

    /**
     * Given a {@link RedCapTrigger}, the function returns the related {@link RedCapInfo}
     *      configuration, if available.
     * @param trigger {@link RedCapTrigger} that has hit the service
     * @return {@link RedCapTrigger} if available
     * @throws IllegalArgumentException in case the there is no valid configuration related the
     *      given {@link RedCapInfo}
     */
    public static RedCapInfo getInfo(RedCapTrigger trigger) {
        return Properties.getRedCapInfo(trigger.getRedcapUrl(), trigger.getProjectId());
    }

    /**
     * Given a {@link String} representing a REDCap form/instrument name, the function returns the
     *      name of the status field for the given REDCap form.
     * @param formName {@link String} containing the name of the form
     * @return {@link String} representing the status field for a REDCap form
     */
    public static String getStatusField(String formName) {
        return RedCapTrigger.getInstrumentStatusField(formName);
    }

    /**
     * Starting from REDCap {@link URL} and REDCap project identifier, the function returns the
     *      {@link ManagementPortalInfo} related to given REDCap project.
     * @param redCapUrl {@link URL} pointing a REDCap instance
     * @param projectId {@link Integer} representing a REDCap project identifier (i.e. pid)
     * @return {@link ManagementPortalInfo} related to a REDCap project
     */
    public static ManagementPortalInfo getRelatedMpInfo(URL redCapUrl, Integer projectId) {
        return Properties.getMpInfo(redCapUrl, projectId);
    }

    /**
     * Given a REDCap {@link URL} and a REDCap project identifier, the function computes the
     *      {@link URL} pointing the integration form for the given REDCap record identifier.
     * @param redCapUrl {@link URL} pointing a REDCap instance. The {@link URL} should contain
     *      the REDCap version as well
     * @param projectId {@link Integer} representing a REDCap project identifier (i.e. pid)
     * @param recordId {@link Integer} stating the REDCap Record identifier for which the
     *      {@link URL} should be computed
     * @return {@link URL} pointing the integration form for the given REDCap Record identifier
     *      within the specified project.
     * @throws MalformedURLException In case the {@link URL} cannot be generated
     */
    public static URL getRecordUrl(URL redCapUrl, Integer projectId, Integer recordId)
            throws MalformedURLException {
        String redCap = Properties.getRedCapInfo(redCapUrl, projectId).getUrl().toString();

        if (redCap.charAt(redCap.length() - 1) != '/') {
            redCap = redCap.concat("/");
        }

        redCap = redCap.concat(PROJECT_ID).concat(projectId.toString());
        redCap = redCap.concat(RECORD_ID).concat(recordId.toString());

        RedCapInfo redCapInfo = Properties.getRedCapInfo(redCapUrl, projectId);

        redCap = redCap.concat(EVENT_ID).concat(redCapInfo.getEnrolmentEvent());
        redCap = redCap.concat(PAGE_NAME).concat(redCapInfo.getIntegrationForm());

        return new URL(redCap);
    }

}
