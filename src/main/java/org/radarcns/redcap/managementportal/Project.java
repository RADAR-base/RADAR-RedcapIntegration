package org.radarcns.redcap.managementportal;

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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.Objects;
import okhttp3.Response;

/**
 * Java class defining a RADAR Management Portal Project.
 */
public class Project {

    /** Label representing the key of the tag stating the REDCap project URL. */
    @JsonIgnore
    public static final String EXTERNAL_PROJECT_URL_KEY = "External-project-url";

    /** Label representing the key of the tag stating the REDCap project identifier. */
    @JsonIgnore
    public static final String EXTERNAL_PROJECT_ID_KEY = "External-project-id";

    /** Label representing the key of the tag stating the project RADAR-CNS work-package. */
    @JsonIgnore
    public static final String WORK_PACKAGE_KEY = "Work-package";

    /** Label representing the key of the tag stating the project phase. */
    @JsonIgnore
    public static final String PHASE_KEY = "Phase";

    private final Integer id;
    private final String projectName;
    private final String organization;
    private final String location;
    private final Map<String, String> attributes;

    protected Project(
            @JsonProperty("id") Integer id,
            @JsonProperty("projectName") String projectName,
            @JsonProperty("organization") String organization,
            @JsonProperty("location") String location,
            @JsonProperty("attributes") Map<String, String> attributes) {
        this.id = id;
        this.projectName = projectName;
        this.organization = organization;
        this.location = location;
        this.attributes = attributes;
    }

    public Integer getId() {
        return id;
    }

    public String getProjectName() {
        return projectName;
    }

    public String getOrganization() {
        return organization;
    }

    public String getLocation() {
        return location;
    }

    public Map<String, String> getAttributes() {
        return attributes;
    }

    /**
     * Returns the value associated with the key {@link #PHASE_KEY}.
     * @return {@link String} reporting the project phase, {@code null} otherwise
     */
    @JsonIgnore
    public String getPhase() {
        return getAttribute(PHASE_KEY);
    }

    /**
     * Returns the value associated with the key {@link #EXTERNAL_PROJECT_URL_KEY}.
     * @return {@link URL} pointing the equivalent REDCap project, {@code null} otherwise
     * @throws MalformedURLException in case the {@link URL} cannot be generated
     */
    @JsonIgnore
    public URL getRedCapUrl() throws MalformedURLException {
        String temp = getAttribute(EXTERNAL_PROJECT_URL_KEY);
        return Objects.isNull(temp) ? null : new URL(temp);
    }

    /**
     * Returns the value associated with the key {@link #EXTERNAL_PROJECT_ID_KEY}.
     * @return {@link Integer} stating the project identifier for the REDCap project equivalent,
     *      {@code null} otherwise
     */
    @JsonIgnore
    public Integer getRedCapId() {
        String temp = getAttribute(EXTERNAL_PROJECT_ID_KEY);
        return Objects.isNull(temp) ? null : Integer.valueOf(temp);
    }

    /**
     * Returns the value associated with the key {@link #WORK_PACKAGE_KEY}.
     * @return {@link String} reporting the RADAR-CNS work-package, {@code null} otherwise
     */
    @JsonIgnore
    public String getWorkPackage() {
        return getAttribute(WORK_PACKAGE_KEY);
    }

    /**
     * Gets the project attribute (e.g. tag) associated with the given {@link String} key.
     * @param key {@link String} tag key
     * @return {@link String} value associated with the given key
     */
    @JsonIgnore
    public String getAttribute(String key) {
       return attributes.get(key);
    }

    /**
     * Converts the {@link Response#body()} to a {@link Project} entity.
     * @param response {@link Response} that has to be converted
     * @return {@link Project} stored in the {@link Response#body()}
     * @throws IOException in case the conversion cannot be computed
     */
    @JsonIgnore
    public static Project getObject(Response response) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        byte[] body = response.body().bytes();
        response.close();
        return mapper.readValue(body, Project.class);
    }

    @Override
    public String toString() {
        return "Project{" + '\n'
            + "id=" + id + '\n'
            + "projectName='" + projectName + "'\n"
            + "organization='" + organization + "'\n"
            + "location='" + location + "'\n"
            + "attributes=" + attributes + '}';
    }
}
