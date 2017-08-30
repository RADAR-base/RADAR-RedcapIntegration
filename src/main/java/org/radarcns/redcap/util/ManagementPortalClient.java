package org.radarcns.redcap.util;

import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Objects;
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
public class ManagementPortalClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(ManagementPortalClient.class);

    private String radarSubjectId;
    private String humanReadableId;

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
     * @param projectUrl {@link URL} pointing the REDCap instance that has issued the trigger
     * @param projectId {@link Integer} representing REDCap project identifier within the trigger
     *      has been generated
     * @param recordId {@link Integer} representing REDCap record identifier involved in the
     *      creation
     */
    public ManagementPortalClient(URL projectUrl, Integer projectId, Integer recordId) {
        Objects.requireNonNull(projectId);
        Objects.requireNonNull(projectUrl);
        Objects.requireNonNull(recordId);

        try {
            //TODO: GET from Management Portal Project details using RECap URL and REDCap projectId

            //TODO: extract information from the just GET project object
            String radarWorkPackage = "MDD";
            String location = "LONDON";

            humanReadableId = radarWorkPackage.concat(projectId.toString()).concat(
                    location).concat(recordId.toString());

            //TODO: PUT subject to the Management Portal. It returns the subject object.
            //What does the PUT return in case the user is already present?

            //TODO: UPDATE subject adding RecordId, HumanReadable Identifier and REDCap project URL.

            radarSubjectId = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss").format(
                    Calendar.getInstance().getTime());

            LOGGER.info("Created RADAR user: {}. Human readable identifier is: {}", radarSubjectId,
                    humanReadableId);
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
}
