package org.radarcns.redcap.config

import com.fasterxml.jackson.annotation.JsonProperty
import org.apache.commons.lang3.builder.EqualsBuilder
import org.apache.commons.lang3.builder.HashCodeBuilder
import org.radarcns.redcap.config.Properties.validateRedcapUrl
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
 */ /**
 * Configuration file entry to define REDCap settings.
 */
data class RedCapInfo @JvmOverloads constructor(
    @JsonProperty("url") var url: URL,
    @JsonProperty("project_id") val projectId: Int,
    @JsonProperty("enrolment_event") val enrolmentEvent: String? = null,
    @JsonProperty("integration_form") val integrationForm: String? = null,
    @JsonProperty("token") val token: String? = null,
    @JsonProperty("attributes") val attributes: Set<Attribute>? = null
) {

    init {
        url = validateRedcapUrl(url)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (other !is RedCapInfo) {
            return false
        }
        val that = other
        return EqualsBuilder()
            .append(url, that.url)
            .append(projectId, that.projectId)
            .isEquals
    }

    override fun hashCode(): Int {
        return HashCodeBuilder(
            17, 37
        )
            .append(url)
            .append(projectId)
            .toHashCode()
    }
}