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

import org.radarcns.redcap.util.RedCapTrigger;

/**
 * Information about the REDCap user used to query the REDCap API.
 */
public final class RedCapManager {

    private RedCapManager() {
        //Static class
    }

    public static boolean isSupportedInstance(RedCapTrigger trigger) {
        return Properties.isSupportedInstance(trigger.getRedcapUrl(), trigger.getProjectId());
    }

    public static RedCapInfo getInfo(RedCapTrigger trigger) {
        return Properties.getRedCapInfo(trigger.getRedcapUrl(), trigger.getProjectId());
    }

    public static String getStatusField(RedCapInfo info) {
        return RedCapTrigger.getInstrumentStatusField(info.getIntegrationForm());
    }

}
