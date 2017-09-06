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
import java.util.Optional;
import okhttp3.Response;

//TODO
public class Subject {

    @JsonIgnore
    public static final String HUMAN_READABLE_IDENTIFIER_KEY = "Human-readable-identifier";

    @JsonProperty("login")
    private final String subjectId;
    private final Integer externalId;
    private final URL externalLink;
    //TODO remove email
    @JsonProperty("email")
    private final String email;
    private final Project project;
    private final List<Tag> attributes;

    /**
     * Constructor.
     * @param subjectId {@link String} representing Management Portal Subject identifier
     * @param externalId {@link Integer} representing the REDCap Record identifier
     * @param externalLink {@link URL} pointing the REDCap integration form / instrument
     * @param project {@link Project} associated with the subject
     * @param attributes {@link List} of {@link Tag}
     */
    public Subject(
            @JsonProperty("login") String subjectId,
            @JsonProperty("externalId") Integer externalId,
            @JsonProperty("externalLink") URL externalLink,
            @JsonProperty("project") Project project,
            @JsonProperty("attributes") List<Tag> attributes) {
        this.subjectId = subjectId;
        this.externalId = externalId;
        this.externalLink = externalLink;
        this.project = project;
        this.attributes = attributes;

        //TODO remove
        this.email = "admin@localhost";
    }

    /**
     * Constructor.
     * @param subjectId {@link String} representing Management Portal Subject identifier
     * @param externalId {@link Integer} representing the REDCap Record identifier
     * @param externalLink {@link URL} pointing the REDCap integration form / instrument
     * @param project {@link Project} associated with the subject
     * @param humanReadableId {@link String} representing the value associated with
     *      {@link #HUMAN_READABLE_IDENTIFIER_KEY}
     */
    public Subject(String subjectId, Integer externalId, URL externalLink, Project project,
            String humanReadableId) {
        this.subjectId = subjectId;
        this.externalId = externalId;
        this.externalLink = externalLink;
        this.project = project;
        this.attributes = Collections.singletonList(new Tag(HUMAN_READABLE_IDENTIFIER_KEY,
                humanReadableId));

        //TODO remove
        this.email = "admin@localhost";
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

    public List<Tag> getAttributes() {
        return attributes;
    }

    /**
     * Returns the Human Readable Identifier associated with this subject.
     * @return {@link String} stating the Human Readable Identifier associated with this subject
     */
    @JsonIgnore
    public String getHumanReadableIdentifier() {
        return getAttribute(HUMAN_READABLE_IDENTIFIER_KEY);
    }

    /**
     * Gets the project attribute (e.g. tag) associated with the given {@link String} key.
     * @param key {@link String} tag key
     * @return {@link String} value associated with the given key
     */
    @JsonIgnore
    public String getAttribute(String key) {
        Optional<Tag> tag = attributes.stream()
                .filter(item -> item.getKey().equals(key)).findFirst();

        return tag.isPresent() ? tag.get().getValue() : null;
    }

    /**
     * Converts the {@link Response#body()} to a {@link Project} entity.
     * @param response {@link Response} that has to be converted
     * @return {@link Project} stored in the {@link Response#body()}
     * @throws IOException in case the conversion cannot be computed
     */
    @JsonIgnore
    public static Subject getObject(Response response) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        byte[] body = response.body().bytes();
        response.close();
        return mapper.readValue(body, Subject.class);
    }

    /**
     * Generates the {@link JsonNode} representation of the current instance.
     * @return {@link JsonNode} serialising this object
     * @throws IOException in case the serialisation cannot be complete
     */
    @JsonIgnore
    public JsonNode getJson() throws IOException {
        return new ObjectMapper().readTree(getJsonString());
    }

    /**
     * Generates the JSON {@link String} representation of the current instance.
     * @return {@link String} serialising this object
     * @throws IOException in case the serialisation cannot be complete
     */
    @JsonIgnore
    public String getJsonString() throws IOException {
        return new ObjectMapper().writeValueAsString(this);
    }
}
