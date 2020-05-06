package org.radarcns.redcap.util

import org.radarcns.redcap.config.RedCapManager
import java.io.IOException
import java.io.UnsupportedEncodingException
import java.net.MalformedURLException
import java.net.URL
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import java.util.*

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
 */ /**
 *
 * Represents data send by REDCap upon triggering.
 *
 * In the REDCap Project Setup view, under `Additional customisations`, it is possible
 * to set a `Data Entry Trigger`. Upon any form or survey creation or update, REDCap
 * automatically triggers a request to the set end point.
 *
 * The sent parameters are
 *  * project_id: The unique ID number of the REDCap project
 * (i.e. the 'pid' value found in the URL when accessing the project in REDCap).
 *  * username: The username of the REDCap user that is triggering the Data Entry Trigger.
 * Note: If it is triggered by a survey page (as opposed to a data entry form), then
 * the username that will be reported will be '[survey respondent]'.
 *  * instrument: The unique name of the current data collection instrument(all your project's
 * unique instrument names can be found in column B in the data dictionary).
 *  * record: The name of the record being created or modified, which is the record's value
 * for the project's first field.
 *  * redcap_event_name: The unique event name of the event for which the record was modified
 * (for longitudinal projects only).
 *  * redcap_data_access_group:  The unique group name of the Data Access Group to which the
 * record belongs (if the record belongs to a group).
 *  * [instrument]_complete:  The status of the record for this particular data collection
 * instrument, in which the value will be 0, 1, or 2. For data entry forms, 0=Incomplete,
 * 1=Unverified, 2=Complete. For surveys, 0=partial survey response and 2=completed survey
 * response. This parameter's name will be the variable name of this particular
 * instrument's status field, which is the name of the instrument + '_complete'.
 *  * redcap_url: The base web address to REDCap (URL of REDCap's home page).
 *  * project_url: The base web address to the current REDCap project (URL of its Project
 * Home page).
 *
 */
class RedCapTrigger(value: String) {
    //private static final Logger LOGGER = LoggerFactory.getLogger(RedCapTrigger.class);
    enum class InstrumentStatus(val status: Int) {
        INCOMPLETE(0), UNVERIFIED(1), COMPLETE(2);
    }

    enum class TriggerParameter(val key: String) {
        PROJECT_ID("project_id"),
        USERNAME("username"),
        INSTRUMENT("instrument"),
        RECORD("record"),
        REDCAP_EVENT_NAME("redcap_event_name"),
        REDCAP_DATA_ACCESS_GROUP("redcap_data_access_group"),
        INSTRUMENT_STATUS("_complete"),
        REDCAP_URL("redcap_url"),
        PROJECT_URL("project_url");

        companion object {
            fun instrumentStatus(instrument: String?): String {
                return instrument + INSTRUMENT_STATUS.name
            }
        }
    }

    var projectId: Int? = null
        private set
    var username: String? = null
        private set
    var instrument: String? = null
        private set
    var record: Int? = null
        private set
    var redcapEventName: String? = null
        private set
    var redcapDataAccessGroup: String? = null
        private set
    var status: InstrumentStatus? = null
        private set
    var projectUrl: URL? = null
        private set
    var redcapUrl: URL? = null
        private set

    /** REDCap provides trigger parameters as a sequence of values separated by &.  */
    @Throws(UnsupportedEncodingException::class, MalformedURLException::class)
    private fun parser(values: Array<String>) {
        var markerIndex: Int
        for (value in values) {
            markerIndex = value.indexOf("=")
            when (convertParameter(value, markerIndex)) {
                TriggerParameter.REDCAP_URL -> redcapUrl = URL(
                    URLDecoder.decode(
                        value.substring(markerIndex + 1).trim { it <= ' ' },
                        StandardCharsets.UTF_8.name()
                    )
                )
                TriggerParameter.PROJECT_URL -> {
                    projectUrl = URL(
                        URLDecoder.decode(
                            value.substring(markerIndex + 1).trim { it <= ' ' },
                            StandardCharsets.UTF_8.name()
                        )
                    )
                    //Override REDCap URL
                    val temp = projectUrl.toString()
                    redcapUrl = URL(temp.substring(0, temp.indexOf("index.php?")))
                }
                TriggerParameter.PROJECT_ID -> projectId =
                    value.substring(markerIndex + 1).trim { it <= ' ' }.toInt()
                TriggerParameter.USERNAME -> username =
                    value.substring(markerIndex + 1).trim { it <= ' ' }
                TriggerParameter.RECORD -> record =
                    value.substring(markerIndex + 1).trim { it <= ' ' }.toInt()
                TriggerParameter.REDCAP_EVENT_NAME -> redcapEventName =
                    value.substring(markerIndex + 1).trim { it <= ' ' }
                TriggerParameter.INSTRUMENT -> instrument =
                    value.substring(markerIndex + 1).trim { it <= ' ' }
                TriggerParameter.REDCAP_DATA_ACCESS_GROUP -> redcapDataAccessGroup =
                    value.substring(markerIndex + 1).trim { it <= ' ' }
                TriggerParameter.INSTRUMENT_STATUS -> status =
                    instrumentStatus(
                        Integer.valueOf(
                            value.substring(markerIndex + 1)
                        )
                    )
                else -> throw IllegalArgumentException("$value cannot be parsed.")
            }
        }
    }

    private fun convertParameter(value: String, markerIndex: Int): TriggerParameter {
        val name = value.substring(0, markerIndex).trim { it <= ' ' }
        for (param in TriggerParameter.values()) {
            if (param.key == name) {
                return param
            }
        }
        if (value.startsWith(TriggerParameter.instrumentStatus(instrument))) {
            return TriggerParameter.INSTRUMENT_STATUS
        }
        throw IllegalArgumentException(" No enum constant for $name")
    }

    /**
     * Checks if the event related to the trigger is the integration event.
     * @return `true` if the event that has triggered the update is the integration one,
     * `false` otherwise.
     */
    val isEnrolment: Boolean
        get() = redcapEventName.equals(
            RedCapManager.getInfo(this).enrolmentEvent, ignoreCase = true
        )

    override fun toString(): String {
        return ("RedCapTrigger{"
                + "projectId='" + projectId + '\''
                + ", username='" + username + '\''
                + ", instrument='" + instrument + '\''
                + ", record='" + record + '\''
                + ", redcapEventName='" + redcapEventName + '\''
                + ", redcapDataAccessGroup='" + redcapDataAccessGroup + '\''
                + ", status=" + status
                + ", redcapUrl='" + redcapUrl + '\''
                + ", projectUrl='" + projectUrl + '\''
                + '}')
    }

    companion object {
        private fun instrumentStatus(value: Int): InstrumentStatus {
            return when (value) {
                0 -> InstrumentStatus.INCOMPLETE
                1 -> InstrumentStatus.UNVERIFIED
                2 -> InstrumentStatus.COMPLETE
                else -> throw IllegalArgumentException(
                    value.toString() + " cannot be converted in "
                            + InstrumentStatus::class.java.name
                )
            }
        }

        /**
         * Gets the status field related to the instrument given in input.
         * @param instrument [String] representing instrument field
         * @return [String] representing status field related to the given instrument
         */
        @JvmStatic
        fun instrumentStatusField(instrument: String): String {
            Objects.requireNonNull(instrument)
            return instrument + TriggerParameter.INSTRUMENT_STATUS.name
        }
    }

    init {
        try {
            val bytes = value.toByteArray(StandardCharsets.UTF_8)
            val input = String(bytes, StandardCharsets.UTF_8)
            parser(input.split("&").toTypedArray())
        } catch (exc: IOException) {
            throw IllegalArgumentException(exc)
        }
    }
}