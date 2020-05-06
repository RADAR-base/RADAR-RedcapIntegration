package org.radarcns.redcap.managementportal

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import okhttp3.Response
import java.io.IOException
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
 * Constructor.
 * @param subjectId [String] representing Management Portal Subject identifier
 * @param externalId [Integer] representing the REDCap Record identifier
 * @param externalLink [URL] pointing the REDCap integration form / instrument
 * @param project [Project] associated with the subject
 * @param attributes [Map] of key,value pairs
 */
data class Subject(
    @JsonProperty("id") val mpId: Long?,
    @JsonProperty("login") val subjectId: String,
    @JsonProperty("externalId") val externalId: Int,
    @JsonProperty("externalLink") val externalLink: URL,
    @JsonProperty("project") val project: Project,
    @JsonProperty("attributes") val attributes: MutableMap<String, String>,
    @JsonProperty("status") val status: String = SubjectStatus.ACTIVATED.toString()
) {
    enum class SubjectStatus {
        DEACTIVATED, ACTIVATED, DISCONTINUED, INVALID
    }

    /**
     * Constructor.
     * @param subjectId [String] representing Management Portal Subject identifier
     * @param externalId [Integer] representing the REDCap Record identifier
     * @param externalLink [URL] pointing the REDCap integration form / instrument
     * @param project [Project] associated with the subject
     * @param humanReadableId [String] representing the value associated with
     * [.HUMAN_READABLE_IDENTIFIER_KEY]
     */
    constructor(
        subjectId: String, externalId: Int, externalLink: URL, project: Project,
        humanReadableId: String
    ) : this(
        null, subjectId, externalId, externalLink, project,
        mutableMapOf<String, String>(Pair(HUMAN_READABLE_IDENTIFIER_KEY, humanReadableId))
    )

    constructor(
        subjectId: String,
        externalId: Int,
        externalLink: URL,
        project: Project,
        humanReadableId: String,
        attributes: Map<String, String>
    ) : this(subjectId, externalId, externalLink, project, humanReadableId) {
        addAttributes(attributes)
    }

    fun addAttributes(attributes: Map<String, String>) {
        this.attributes.putAll(attributes)
    }

    fun attributes(): Map<String, String> {
        return attributes
    }


    /**
     * Returns the Human Readable Identifier associated with this subject.
     * @return [String] stating the Human Readable Identifier associated with this subject
     */
    @get:JsonIgnore
    val humanReadableIdentifier: String?
        get() = attributes[HUMAN_READABLE_IDENTIFIER_KEY]

    /**
     * Generates the [JsonNode] representation of the current instance.
     * @return [JsonNode] serialising this object
     * @throws IOException in case the serialisation cannot be complete
     */
    @get:Throws(IOException::class)
    @get:JsonIgnore
    val json: JsonNode
        get() = ObjectMapper().readTree(jsonString)

    /**
     * Generates the JSON [String] representation of the current instance.
     * @return [String] serialising this object
     * @throws IOException in case the serialisation cannot be complete
     */
    @get:Throws(IOException::class)
    @get:JsonIgnore
    val jsonString: String
        get() = ObjectMapper().writeValueAsString(this)

    companion object {
        const val HUMAN_READABLE_IDENTIFIER_KEY = "Human-readable-identifier"

        private val mapper = ObjectMapper().also {
            it.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            it.registerModule(KotlinModule())
        }

        /**
         * Converts the [Response.body] to a [List] of [Subject] entity.
         * @param response [Response] that has to be converted
         * @return [Subject] stored in the [Response.body]
         * @throws IOException in case the conversion cannot be computed
         */
        @Throws(IOException::class)
        fun subjects(response: Response): List<Subject> {
            val body = response.body()!!.bytes()
            response.close()
            return mapper.readValue(
                body,
                object : TypeReference<List<Subject>>() {})
        }

        /**
         * Converts the [Response.body] to a [Project] entity.
         * @param response [Response] that has to be converted
         * @return [Project] stored in the [Response.body]
         * @throws IOException in case the conversion cannot be computed
         */
        @Throws(IOException::class)
        fun subject(response: Response): Subject {
            val body = response.body()!!.bytes()
            response.close()
            return mapper.readValue(
                body,
                Subject::class.java
            )
        }
    }
}