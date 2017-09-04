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

import com.fasterxml.jackson.annotation.JsonProperty;
import java.net.URL;

//TODO
public class RedCapInfo {

    private final URL url;
    private final Integer projectId;
    private final String enrolmentEvent;
    private final String integrationForm;
    private final String token;

    protected RedCapInfo(URL url, Integer projectId) {
        this.url = url;
        this.projectId = projectId;
        this.enrolmentEvent = null;
        this.integrationForm = null;
        this.token = null;
    }

    protected RedCapInfo(
            @JsonProperty("url") URL url,
            @JsonProperty("project_id") Integer projectId,
            @JsonProperty("enrolment_event") String enrolmentEvent,
            @JsonProperty("integration_form") String integrationForm,
            @JsonProperty("token")String token) {
        this.url = url;
        this.projectId = projectId;
        this.enrolmentEvent = enrolmentEvent;
        this.integrationForm = integrationForm;
        this.token = token;
    }

    /**
     * Returns URL pointing the REDCap instance.
     * @return {@link URL} pointing a REDCap instance
     */
    public URL getUrl() {
        return url;
    }

    /**
     * Returns the unique project identifier of the REDCap project.
     * @return {@link Integer} representing REDCap unique event identifier
     */
    public Integer getProjectId() {
        return projectId;
    }

    /**
     * Returns API Token used to identify the REDCap user against the REDCap instance.
     * @return {@link String} representing REDCap API Token
     */
    public String getToken() {
        return token;
    }

    /**
     * Returns the unique event name of the REDCap event related to the enrolment process.
     * @return {@link String} representing REDCap unique event identifier
     */
    public String getEnrolmentEvent() {
        return enrolmentEvent;
    }

    /**
     * Returns the name of the form used to couple REDCap and Management Portal projects.
     * @return {@link String} representing REDCap instrument name
     */
    public String getIntegrationForm() {
        return integrationForm;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof RedCapInfo)) {
            return false;
        }

        RedCapInfo that = (RedCapInfo) obj;

        return new org.apache.commons.lang3.builder.EqualsBuilder()
            .append(url, that.url)
            .append(projectId, that.projectId)
            .isEquals();
    }

    @Override
    public int hashCode() {
        return new org.apache.commons.lang3.builder.HashCodeBuilder(
                17, 37)
            .append(url)
            .append(projectId)
            .toHashCode();
    }

    @Override
    public String toString() {
        return "RedCapInfo {" + "\n"
            + "url = " + url + "\n"
            + "projectId = " + projectId + "\n"
            + "enrolmentEvent = '" + enrolmentEvent + "'\n"
            + "integrationForm = '" + integrationForm + "'\n"
            + "token = '" + token + "'" + '}' + "\n";
    }
}
