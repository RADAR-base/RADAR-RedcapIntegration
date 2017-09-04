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
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import okhttp3.Response;

//TODO
public class Subject {

    @JsonIgnore
    public static final String HUMAN_READABLE_IDENTIFIER_KEY = "Human-readable-identifier";

    @JsonProperty("login")
    private final String subjectId;
    private final Integer externalId;
    private final URL externalLink;
    private final String email;
    private final Project project;
    private final List<Tag> attributes;

    public Subject(
            @JsonProperty("login") String subjectId,
            @JsonProperty("externalId") Integer externalId,
            @JsonProperty("externalLink") URL externalLink,
            @JsonProperty("email") String email,
            @JsonProperty("project") Project project,
            @JsonProperty("attributes") List<Tag> attributes) {
        this.subjectId = subjectId;
        this.externalId = externalId;
        this.externalLink = externalLink;
        this.email = email;
        this.project = project;
        this.attributes = attributes;
    }

    public Subject(String subjectId, Integer externalId, URL externalLink, String email,
            Project project, String humanReadableId) {
        this.subjectId = subjectId;
        this.externalId = externalId;
        this.externalLink = externalLink;
        this.email = email;
        this.project = project;
        this.attributes = Collections.singletonList(new Tag(HUMAN_READABLE_IDENTIFIER_KEY,
                humanReadableId));
    }

    public String getSubjectId() {
        return subjectId;
    }

    public Integer getExternalId() {
        return externalId;
    }

    public URL getExternalLink() {
        return externalLink;
    }

    public String getEmail() {
        return email;
    }

    public Project getProject() {
        return project;
    }

    @JsonIgnore
    public static Subject getObject(Response response) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return mapper.readValue(response.body().bytes(), Subject.class);
    }

    @JsonIgnore
    public JsonNode getJson() throws IOException {
        return new ObjectMapper().readTree(getJsonString());
    }

    @JsonIgnore
    public String getJsonString() throws IOException {
        return new ObjectMapper().writeValueAsString(this);
    }
}
