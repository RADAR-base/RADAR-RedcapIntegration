package org.radarcns.redcap.managementportal

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import org.radarcns.exception.TokenException
import org.radarcns.oauth.OAuth2Client
import org.radarcns.redcap.config.Properties
import org.radarcns.redcap.config.RedCapManager
import org.slf4j.LoggerFactory
import java.io.IOException
import java.net.MalformedURLException
import java.net.URI
import java.net.URISyntaxException
import java.net.URL
import java.time.Duration
import java.util.*
import javax.inject.Inject
import javax.ws.rs.core.MediaType

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
 * Client to interact with the RADAR Management Portal.
 */
open class MpClient @Inject constructor(private val httpClient: OkHttpClient) {

    private val oauthClient: OAuth2Client by lazy {
        OAuth2Client.Builder()
            .credentials(Properties.oauthClientId, Properties.oauthClientSecret)
            .endpoint(Properties.tokenEndPoint)
            .httpClient(httpClient).build()
    }

    @get:Throws(IOException::class)
    private val token: String
        get() = try {
            oauthClient.getValidToken(Duration.ofSeconds(30)).accessToken
        } catch (ex: TokenException) {
            throw IOException(ex)
        }

    @Throws(IOException::class)
    fun getProject(redcapUrl: URL, projectId: Int): Project {
        val mpInfo = RedCapManager.getRelatedMpInfo(redcapUrl, projectId)
        val request =
            getBuilder(Properties.getProjectEndPoint(mpInfo)).get()
                .build()
        try {
            httpClient.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val project = Project.project(response)
                    validateProject(
                        Properties.getProjectEndPoint(mpInfo),
                        project, redcapUrl, projectId
                    )
                    LOGGER.debug("Retrieve project {}", project.toString())
                    return project
                }
                throw IllegalStateException(
                    "Error while retrieving project info from "
                            + Properties.getProjectEndPoint(mpInfo) + ". Response code: "
                            + response.code() + " Message: " + response.message()
                )
            }
        } catch (ex: IOException) {
            throw IllegalStateException(
                "IO error while retrieving project info from "
                        + Properties.getProjectEndPoint(mpInfo) + ".",
                ex
            )
        }
    }

    fun updateSubject(subject: Subject): Subject {
        return try {
            val request =
                getBuilder(Properties.subjectEndPoint)
                    .put(
                        RequestBody.create(
                            okhttp3.MediaType.parse(MediaType.APPLICATION_JSON), subject.jsonString
                        )
                    )
                    .build()
            val response = httpClient.newCall(request).execute()
            if (response.isSuccessful) {
                LOGGER.debug(
                    "Successfully updated subject: {}",
                    subject.jsonString
                )
                subject
            } else {
                throw IllegalStateException(
                    "Subject cannot be updated. Response code: "
                            + response.code() + " Message: " + response.message() + " Info: "
                            + response.body()!!.string()
                )
            }
        } catch (exc: IOException) {
            throw IllegalStateException("Subject cannot be created", exc)
        }
    }

    fun createSubject(
        redcapUrl: URL,
        project: Project,
        recordId: Int,
        humanReadableId: String,
        attributes: Map<String, String>
    ): Subject { //TODO check how to generate UUID
        return try {
            val radarSubjectId = UUID.randomUUID().toString()
            val subject =
                Subject(
                    radarSubjectId, recordId,
                    RedCapManager.getRecordUrl(redcapUrl, project.redCapId!!, recordId),
                    project, humanReadableId, attributes
                )
            val request =
                getBuilder(Properties.subjectEndPoint)
                    .post(
                        RequestBody.create(
                            okhttp3.MediaType.parse(
                                MediaType.APPLICATION_JSON
                            ), subject.jsonString
                        )
                    )
                    .build()
            val response = httpClient.newCall(request).execute()
            if (response.isSuccessful) {
                LOGGER.debug(
                    "Successfully created subject: {}",
                    subject.jsonString
                )
                subject
            } else {
                throw IllegalStateException(
                    "Subject cannot be created. Response code: "
                            + response.code() + " Message: " + response.message() + " Info: "
                            + response.body()!!.string()
                )
            }
        } catch (exc: IOException) {
            throw IllegalStateException("Subject cannot be created", exc)
        }
    }

    @Throws(IOException::class, URISyntaxException::class)
    fun getSubject(
        redcapUrl: URL,
        projectId: Int,
        recordId: Int
    ): Subject? {
        val mpInfo = RedCapManager.getRelatedMpInfo(redcapUrl, projectId)
        val request = getBuilder(
            getSubjectUrl(
                Properties.subjectEndPoint,
                mpInfo.projectName, recordId
            )
        ).get().build()
        try {
            httpClient.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val subjects =
                        Subject.subjects(response)
                    check(subjects.size <= 1) {
                        ("More than 1 subjects exist with same "
                                + "externalId in the same Project")
                    }
                    return subjects[0]
                }
                LOGGER.info("Subject is not present")
                return null
            }
        } catch (exc: IOException) {
            throw IllegalStateException("Subject could not be retrieved", exc)
        }
    }

    @Throws(IOException::class)
    private fun getBuilder(url: URL): Request.Builder {
        return Request.Builder()
            .url(url)
            .addHeader("Authorization", "Bearer $token")
    }

    companion object {
        private val LOGGER =
            LoggerFactory.getLogger(MpClient::class.java)

        private fun validateProject(
            mp: URL, project: Project,
            redcapUrl: URL, projectId: Int
        ) {
            val message = ("Project " + project.id + " at "
                    + mp.toString() + " seems to be not linked to the REDCap project " + projectId
                    + " at " + redcapUrl + ". Please check values set for "
                    + Project.EXTERNAL_PROJECT_URL_KEY + " and " + Project.EXTERNAL_PROJECT_ID_KEY
                    + " for Project " + project.id + " at " + mp.toString())
            try {
                require(
                    !(project.redCapId != projectId
                            || project.redCapUrl == redcapUrl)
                ) { message }
            } catch (exc: MalformedURLException) {
                throw IllegalArgumentException(message + " " + exc.message)
            }
        }

        @Throws(URISyntaxException::class, MalformedURLException::class)
        private fun getSubjectUrl(
            url: URL,
            projectName: String,
            recordId: Int
        ): URL {
            val oldUri = url.toURI()
            val parameters = "projectName=" + projectName + "&externalId=" + recordId.toString()
            var newQuery = oldUri.query
            if (newQuery == null) {
                newQuery = parameters
            } else {
                newQuery += parameters
            }
            val newUri = URI(
                oldUri.scheme, oldUri.authority, oldUri.path, newQuery, oldUri.fragment
            )
            LOGGER.info("URI = $newUri")
            return newUri.toURL()
        }
    }
}