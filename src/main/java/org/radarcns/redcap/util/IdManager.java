package org.radarcns.redcap.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import org.radarcns.redcap.enrolment.RadarEnrolment;
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
 * Provides the {@code RADAR Subject Identifier} and the relative {@code Human Readable identifier}
 *      interacting with the Management Portal.
 */
public class IdManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(RadarEnrolment.class);

    private String radarId;
    private String humanReadableId;

    /**
     * Constructor.
     * @param trigger {@link RedCapTrigger} containing the needed information for identifying
     *      the involved project and record.
     */
    public IdManager(RedCapTrigger trigger) {
        //TODO call Management Portal to create user and retrieve the created RadarId and tags
        // useful to compute the human readable id
        //TODO throw a runtime exception in case of errors

        radarId = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss").format(
                Calendar.getInstance().getTime());

        humanReadableId = "MDD-1-LONDON-".concat(trigger.getRecord().toString());

        LOGGER.info("Created RADAR user: {}. Human readable identifier is: {}", radarId,
                humanReadableId);
    }

    public String getRadarId() {
        return radarId;
    }

    public String getHumanReadableId() {
        return humanReadableId;
    }
}
