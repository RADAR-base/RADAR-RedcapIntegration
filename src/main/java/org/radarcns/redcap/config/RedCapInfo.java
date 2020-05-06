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
import java.util.Set;

/**
 * Configuration file entry to define REDCap settings.
 */
public class RedCapInfo {

    /** URL pointing a REDCap instance. */
    @JsonProperty("url")
    private URL url;

    /**
     * Integer representing the REDCap project identifier. In REDCap the project
     *      is usually called {@code pid}. */
    @JsonProperty("project_id")
    private Integer projectId;

    /**
     * String stating the unique REDCap event name related to the enrolment event. For
     *      each event in REDCap there are to different identifiers: the {@code event_id} that is
     *      an Integer and the {@code event_name} that is a String.
     *      This variable must be equals to the {@code event_name}.
     */
    @JsonProperty("enrolment_event")
    private String enrolmentEvent;

    /**
     * String reporting the REDCap form / instrument name used to share identifiers.
     */
    @JsonProperty("integration_form")
    private String integrationForm;

    /**
     * Token String required to use REDCap APIs.
     */
    @JsonProperty("token")
    private String token;

    /**
     * Token String required to use REDCap APIs.
     */
    @JsonProperty("attributes")
    private Set<Attribute> attributes;

    public RedCapInfo() {
        // for deserialization
    }

    protected RedCapInfo(URL url, Integer projectId) {
        this.url = url;
        this.projectId = projectId;
        this.enrolmentEvent = null;
        this.integrationForm = null;
        this.token = null;
        this.attributes = null;
    }

    protected RedCapInfo(URL url, Integer projectId,
            String enrolmentEvent, String integrationForm,
            String token, Set<Attribute> attributes) {
        this.url = url;
        this.projectId = projectId;
        this.enrolmentEvent = enrolmentEvent;
        this.integrationForm = integrationForm;
        this.token = token;
        this.attributes = attributes;
    }

    /**
     * Returns URL pointing the REDCap instance.
     * @return {@link URL} pointing a REDCap instance
     */
    public URL getUrl() {
        return Properties.validateRedcapUrl(url);
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

    public Set<Attribute> getAttributes() { return attributes; }

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
            + "token = '" + token + "'" + '}';
    }
}
