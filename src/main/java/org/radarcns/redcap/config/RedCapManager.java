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
     * Given a {@link RedCapInfo}, the function returns the
     * @param info TODO
     * @return TODO
     */
    public static String getStatusField(RedCapInfo info) {
        return RedCapTrigger.getInstrumentStatusField(info.getIntegrationForm());
    }

    /**
     * TODO.
     * @param redCapUrl TODO
     * @param projectId TODO
     * @return TODO
     */
    public static ManagementPortalInfo getRelatedMpInfo(URL redCapUrl, Integer projectId) {
        return Properties.getMpInfo(redCapUrl, projectId);
    }

    /**
     * TODO.
     * @param redCapUrl TODO
     * @param projectId TODO
     * @param recordId TODO
     * @return TODO
     * @throws MalformedURLException TODO
     */
    public static URL getRecordUrl(URL redCapUrl, Integer projectId, Integer recordId)
            throws MalformedURLException {
        String redCap = Properties.getRedCapInfo(redCapUrl, projectId).getUrl().toString();

        if (redCap.charAt(redCap.length() - 1) != '/') {
            redCap = redCap.concat("/");
        }

        redCap = redCap.concat("DataEntry/index.php?pid=").concat(projectId.toString());
        redCap = redCap.concat("&id=").concat(recordId.toString());

        RedCapInfo redCapInfo = Properties.getRedCapInfo(redCapUrl, projectId);

        redCap = redCap.concat("&event_id=").concat(redCapInfo.getEnrolmentEvent());
        redCap = redCap.concat("&page=").concat(redCapInfo.getIntegrationForm());

        return new URL(redCap);
    }

}
