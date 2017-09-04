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
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import okhttp3.Response;

//TODO
public class Project {

    @JsonIgnore
    public static final String EXTERNAL_PROJECT_URL_KEY = "External-project-url";
    @JsonIgnore
    public static final String EXTERNAL_PROJECT_ID_KEY = "External-project-id";
    @JsonIgnore
    public static final String WORK_PACKAGE_KEY = "Work-package";
    @JsonIgnore
    public static final String PHASE_KEY = "Phase";

    private final Integer id;
    private final String projectName;
    private final String organization;
    private final String location;
    private final List<Tag> attributes;

    public Project(
        @JsonProperty("id") Integer id,
        @JsonProperty("projectName") String projectName,
        @JsonProperty("organization") String organization,
        @JsonProperty("location") String location,
        @JsonProperty("attributes") List<Tag> attributes) {
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

    public List<Tag> getAttributes() {
        return attributes;
    }

    @JsonIgnore
    public String getPhase() {
        return getAttribute(PHASE_KEY);
    }

    @JsonIgnore
    public URL getRedCapUrl() throws MalformedURLException {
        String temp = getAttribute(EXTERNAL_PROJECT_URL_KEY);
        return Objects.isNull(temp) ? null : new URL(temp);
    }

    @JsonIgnore
    public Integer getRedCapId() {
        String temp = getAttribute(EXTERNAL_PROJECT_ID_KEY);
        return Objects.isNull(temp) ? null : Integer.valueOf(temp);
    }

    @JsonIgnore
    public String getWorkPackage() {
        return getAttribute(WORK_PACKAGE_KEY);
    }

    @JsonIgnore
    public String getAttribute(String key) {
        Optional<Tag> tag = attributes.stream()
            .filter(item -> item.getKey().equals(key)).findFirst();

        return tag.isPresent() ? tag.get().getValue() : null;
    }

    @JsonIgnore
    public static Project getObject(Response response) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return mapper.readValue(response.body().bytes(), Project.class);
    }
}
