package org.radarbase.redcap.managementportal

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import okhttp3.Response
import java.io.IOException
import java.net.MalformedURLException
import java.net.URL
import org.radarbase.redcap.managementportal.Organization

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
 * Java class defining a RADAR Management Portal Project.
 */
data class Project(
    @JsonProperty("id") val id: Int,
    @JsonProperty("projectName") val projectName: String,
    @JsonProperty("organization") @JsonDeserialize(using = OrganizationDeserializer::class) val organization: Organization,
    @JsonProperty("location") val location: String = "",
    @JsonProperty("attributes") val attributes: Map<String, String> = emptyMap()
) {

    /**
     * Returns the value associated with the key [.PHASE_KEY].
     * @return [String] reporting the project phase, `null` otherwise
     */
    @get:JsonIgnore
    val phase: String?
        get() = attribute(PHASE_KEY)

    /**
     * Returns the value associated with the key [.EXTERNAL_PROJECT_URL_KEY].
     * @return [URL] pointing the equivalent REDCap project, `null` otherwise
     * @throws MalformedURLException in case the [URL] cannot be generated
     */
    @get:Throws(MalformedURLException::class)
    @get:JsonIgnore
    val redCapUrl: URL?
        get() = attribute(EXTERNAL_PROJECT_URL_KEY)?.let { URL(it) }


    /**
     * Returns the value associated with the key [.EXTERNAL_PROJECT_ID_KEY].
     * @return [Integer] stating the project identifier for the REDCap project equivalent,
     * `null` otherwise
     */
    @get:JsonIgnore
    @get:Throws(NumberFormatException::class)
    val redCapId: Int?
        get() = attribute(EXTERNAL_PROJECT_ID_KEY)?.toInt()

    /**
     * Returns the value associated with the key [.WORK_PACKAGE_KEY].
     * @return [String] reporting the RADAR-CNS work-package, `null` otherwise
     */
    @get:JsonIgnore
    val workPackage: String?
        get() = attribute(WORK_PACKAGE_KEY)

    /**
     * Gets the project attribute (e.g. tag) associated with the given [String] key.
     * @param key [String] tag key
     * @return [String] value associated with the given key
     */
    @JsonIgnore
    fun attribute(key: String): String? {
        return attributes[key]
    }

    companion object {
        /** Label representing the key of the tag stating the REDCap project URL.  */
        const val EXTERNAL_PROJECT_URL_KEY = "External-project-url"
        /** Label representing the key of the tag stating the REDCap project identifier.  */
        const val EXTERNAL_PROJECT_ID_KEY = "External-project-id"
        /** Label representing the key of the tag stating the project RADAR-CNS work-package.  */
        const val WORK_PACKAGE_KEY = "Work-package"
        /** Label representing the key of the tag stating the project phase.  */
        const val PHASE_KEY = "Phase"

        private val mapper = ObjectMapper().apply {
            configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            registerModule(KotlinModule(nullIsSameAsDefault = true))
        }

        /**
         * Converts the [Response.body] to a [Project] entity.
         * @param response [Response] that has to be converted
         * @return [Project] stored in the [Response.body]
         * @throws IOException in case the conversion cannot be computed
         */
        @Throws(IOException::class)
        fun project(response: Response): Project {
            val body = response.body()!!.bytes()
            response.close()
            return mapper.readValue(body, Project::class.java)
        }
    }

}