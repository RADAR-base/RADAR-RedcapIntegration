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
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URL;
import java.util.*;

import okhttp3.Response;

public class Subject {

    public enum SubjectStatus {
        DEACTIVATED,
        ACTIVATED,
        DISCONTINUED,
        INVALID
    }

    public enum SubjectOperationStatus {
        UPDATED,
        CREATED,
        FAILED,
        OTHER
    }

    @JsonIgnore
    public static final String HUMAN_READABLE_IDENTIFIER_KEY = "Human-readable-identifier";

    @JsonProperty("id")
    private final Long mpId;
    
    @JsonProperty("login")
    private final String subjectId;
    private final Integer externalId;
    private final URL externalLink;
    //TODO remove email
    @JsonProperty("email")
    private final String email;
    private final Project project;
    private final Map<String,String> attributes;

    @JsonProperty("status")
    private final String status;

    @JsonIgnore
    private SubjectOperationStatus operationStatus;


    /**
     * Constructor.
     * @param subjectId {@link String} representing Management Portal Subject identifier
     * @param externalId {@link Integer} representing the REDCap Record identifier
     * @param externalLink {@link URL} pointing the REDCap integration form / instrument
     * @param project {@link Project} associated with the subject
     * @param attributes {@link Map} of key,value pairs
     */
    public Subject(
            @JsonProperty("id") Long mpId,
            @JsonProperty("login") String subjectId,
            @JsonProperty("externalId") Integer externalId,
            @JsonProperty("externalLink") URL externalLink,
            @JsonProperty("project") Project project,
            @JsonProperty("attributes") Map<String,String> attributes) {
        this.mpId = mpId;
        this.subjectId = subjectId;
        this.externalId = externalId;
        this.externalLink = externalLink;
        this.project = project;
        this.attributes = attributes;
        this.status = SubjectStatus.ACTIVATED.toString();

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
        this.mpId = null;
        this.subjectId = subjectId;
        this.externalId = externalId;
        this.externalLink = externalLink;
        this.project = project;
        this.status = SubjectStatus.ACTIVATED.toString();

        Map<String,String> att = new HashMap<>();
        att.put(HUMAN_READABLE_IDENTIFIER_KEY, humanReadableId);
        this.attributes = att;

        //TODO remove
        this.email = "admin@localhost";
    }

    public Subject(String subjectId, Integer externalId, URL externalLink, Project project,
                   String humanReadableId, Map<String, String> attributes) {
        this(subjectId, externalId, externalLink, project, humanReadableId);
        this.setAttributes(attributes);
    }

    public void setAttributes(Map<String, String> attributes) {
        this.attributes.putAll(attributes);
    }

    public void setOprationStatus(SubjectOperationStatus operationStatus) {
        this.operationStatus = operationStatus;
    }

    public Long getMpId() { return mpId; }

    public String getStatus() { return status; }

    public SubjectOperationStatus getOprationStatus() { return operationStatus; }

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

    public Map<String, String> getAttributes() {
        return attributes;
    }

    /**
     * Returns the Human Readable Identifier associated with this subject.
     * @return {@link String} stating the Human Readable Identifier associated with this subject
     */
    @JsonIgnore
    public String getHumanReadableIdentifier() {
        return attributes.get(HUMAN_READABLE_IDENTIFIER_KEY);
    }

    /**
     * Converts the {@link Response#body()} to a {@link List} of {@link Subject} entity.
     * @param response {@link Response} that has to be converted
     * @return {@link Subject} stored in the {@link Response#body()}
     * @throws IOException in case the conversion cannot be computed
     */
    @JsonIgnore
    public static List<Subject> getObjects(Response response) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        byte[] body = response.body().bytes();
        response.close();
        return mapper.readValue(body, new TypeReference<List<Subject>>(){});
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
