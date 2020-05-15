package org.radarcns.redcap.config

import org.radarcns.redcap.config.Properties.getMpInfo
import org.radarcns.redcap.config.Properties.getRedCapInfo
import org.radarcns.redcap.config.Properties.isSupportedInstance
import org.radarcns.redcap.util.RedCapTrigger
import org.radarcns.redcap.util.RedCapTrigger.Companion.instrumentStatusField
import java.net.MalformedURLException
import java.net.URL

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
 * Information about the REDCap user used to query the REDCap API.
 */
object RedCapManager {
    private const val PROJECT_ID = "DataEntry/index.php?pid="
    private const val RECORD_ID = "&id="
    private const val EVENT_ID = "&event_id="
    private const val PAGE_NAME = "&page="

    /**
     * Given a [RedCapTrigger], the function checks whether there is a valid configuration
     * for the couple REDCap URL instance and REDCap project identifier specified in the
     * trigger. If the configuration is available, it returns `true`. it returns
     * `false` otherwise.
     * @param trigger [RedCapTrigger] that has hit the service
     * @return `true` if a valid configuration is available to handle this trigger,
     * `false` otherwise
     */
    fun isSupportedInstance(trigger: RedCapTrigger): Boolean {
        return isSupportedInstance(
            trigger.redcapUrl!!,
            trigger.projectId!!
        )
    }

    /**
     * Given a [RedCapTrigger], the function returns the related [RedCapInfo]
     * configuration, if available.
     * @param trigger [RedCapTrigger] that has hit the service
     * @return [RedCapTrigger] if available
     * given [RedCapInfo]
     */
    fun getInfo(trigger: RedCapTrigger): RedCapInfo {
        return getRedCapInfo(
            trigger.redcapUrl!!,
            trigger.projectId!!
        )
    }

    /**
     * Given a [String] representing a REDCap form/instrument name, the function returns the
     * name of the status field for the given REDCap form.
     * @param formName [String] containing the name of the form
     * @return [String] representing the status field for a REDCap form
     */
    fun getStatusField(formName: String?): String {
        return instrumentStatusField(formName!!)
    }

    /**
     * Starting from REDCap [URL] and REDCap project identifier, the function returns the
     * [ManagementPortalInfo] related to given REDCap project.
     * @param redCapUrl [URL] pointing a REDCap instance
     * @param projectId [Integer] representing a REDCap project identifier (i.e. pid)
     * @return [ManagementPortalInfo] related to a REDCap project
     */
    fun getRelatedMpInfo(redCapUrl: URL?, projectId: Int?): ManagementPortalInfo {
        return getMpInfo(redCapUrl!!, projectId!!)
    }

    /**
     * Given a REDCap [URL] and a REDCap project identifier, the function computes the
     * [URL] pointing the integration form for the given REDCap record identifier.
     * @param redCapUrl [URL] pointing a REDCap instance. The [URL] should contain
     * the REDCap version as well
     * @param projectId [Integer] representing a REDCap project identifier (i.e. pid)
     * @param recordId [Integer] stating the REDCap Record identifier for which the
     * [URL] should be computed
     * @return [URL] pointing the integration form for the given REDCap Record identifier
     * within the specified project.
     * @throws MalformedURLException In case the [URL] cannot be generated
     * @throws NoSuchElementException In case the [URL] does not contain any characters
     */
    @Throws(MalformedURLException::class, NoSuchElementException::class)
    fun getRecordUrl(redCapUrl: URL, projectId: Int, recordId: Int): URL {
        val redCapInfo = getRedCapInfo(redCapUrl, projectId)
        var redCap = redCapInfo.url.toString()

        if (redCap.last() != '/') {
            redCap = "$redCap/"
        }

        redCap = "$redCap$PROJECT_ID$projectId$RECORD_ID$recordId" +
                "$EVENT_ID${redCapInfo.enrolmentEvent}$PAGE_NAME${redCapInfo.integrationForm}"
        return URL(redCap)
    }
}