package org.radarbase.redcap.managementportal

import okhttp3.MediaType.parse
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import org.radarbase.redcap.config.Properties
import org.radarbase.redcap.config.RedCapManager
import org.radarbase.redcap.webapp.exception.IllegalRequestException
import org.radarbase.redcap.webapp.exception.SubjectOperationException
import org.radarcns.exception.TokenException
import org.radarcns.oauth.OAuth2Client
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

    @get:Throws(TokenException::class)
    private val token: String
        get() {
            LOGGER.info("Trying to get token from {}", oauthClient.tokenEndpoint.toString())
            return oauthClient.getValidToken(Duration.ofSeconds(30)).accessToken
        }

    @Throws(IOException::class)
    fun getProject(redcapUrl: URL, projectId: Int): Project {
        val projectEndpoint =
            Properties.getProjectEndPoint(
                RedCapManager.getRelatedMpInfo(redcapUrl, projectId))
        val request = getBuilder(projectEndpoint).get().build()
        val errorMessage = "Error while retrieving project info from $projectEndpoint."

        return performRequest(
            request = request,
            onSuccess = { response ->
                val project =
                    Project.project(
                        response
                    )
                validateProject(
                    projectEndpoint,
                    project,
                    redcapUrl,
                    projectId
                )
                LOGGER.debug("Retrieved project {}", project.toString())
                project
            },
            onError = { response ->
                throw SubjectOperationException(
                    errorMessage +
                            " Response code: ${response.code()} Message: ${response.message()}"
                )
            },
            errorMessage = errorMessage
        ) ?: throw IOException(errorMessage)
    }

    fun updateSubject(subject: Subject): Subject {
        val request =
            getBuilder(Properties.subjectEndPoint)
                .put(RequestBody.create(parse(MediaType.APPLICATION_JSON), subject.jsonString))
                .build()
        val errorMessage = "Subject cannot be updated."
        return performRequest(
            request = request,
            onSuccess = {
                LOGGER.debug(
                    "Successfully updated subject: {}",
                    subject.jsonString
                )
                subject
            },
            onError = { response ->
                throw SubjectOperationException(
                    errorMessage + "Response code: " + response.code() +
                            " Message: " + response.message() +
                            " Info: " + response.body()!!.string()
                )
            },
            errorMessage = errorMessage
        ) ?: throw IOException(errorMessage)
    }

    fun createSubject(
        redcapUrl: URL,
        project: Project,
        recordId: Int,
        humanReadableId: String,
        attributes: Map<String, String>
    ): Subject {

        val radarSubjectId = UUID.randomUUID().toString()
        val subject = Subject(
            subjectId = radarSubjectId,
            externalId = recordId,
            externalLink = RedCapManager.getRecordUrl(
                redcapUrl,
                project.redCapId!!,
                recordId
            ),
            project = project,
            humanReadableId = humanReadableId,
            attributes = attributes
        )
        val request =
            getBuilder(Properties.subjectEndPoint).post(
                RequestBody.create(
                    parse(MediaType.APPLICATION_JSON), subject.jsonString
                )
            ).build()

        val errorMessage = "Subject cannot be created."

        return performRequest(
            request = request,
            onSuccess = {
                LOGGER.debug(
                    "Successfully created subject: {}",
                    subject.jsonString
                )
                subject
            },
            onError = { response ->
                throw SubjectOperationException(
                    errorMessage + " Response code: " + response.code() +
                            ", Message: " + response.message() +
                            ", Info: " + response.body()!!.string()
                )
            },
            errorMessage = errorMessage
        ) ?: throw IOException("Subject cannot be created")
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
                mpInfo.projectName,
                recordId
            )
        ).get().build()

        return performRequest(
            request = request,
            onSuccess = { response ->
                val subjects =
                    Subject.subjects(
                        response
                    )
                check(subjects.size <= 1) {
                    ("More than 1 subjects exist with same "
                            + "externalId in the same Project")
                }
                subjects[0]
            },
            onError = {
                LOGGER.info("Subject is not present")
                null
            },
            errorMessage = "Subject could not be retrieved"
        )
    }

    @Throws(IOException::class)
    private fun <T> performRequest(
        request: Request,
        onSuccess: (Response) -> T,
        onError: (Response) -> T? = { throw IOException("Error when performing request: $it") },
        errorMessage: String = "Problem performing operation on Entity."
    ): T? {

        return try {
            httpClient.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    onSuccess(response)
                } else {
                    onError(response)
                }
            }
        } catch (ex: IOException) {
            throw IOException(errorMessage, ex)
        }
    }

    @Throws(IOException::class)
    private fun getBuilder(url: URL) =
        Request.Builder().url(url).addHeader("Authorization", "Bearer $token")

    companion object {
        private val LOGGER = LoggerFactory.getLogger(MpClient::class.java)

        private fun validateProject(mp: URL, project: Project, redcapUrl: URL, projectId: Int) {
            val message = ("Project " + project.id + " at "
                    + mp.toString() + " seems to be not linked to the REDCap project " + projectId
                    + " at " + redcapUrl + ". Please check values set for "
                    + Project.EXTERNAL_PROJECT_URL_KEY + " and " + Project.EXTERNAL_PROJECT_ID_KEY
                    + " for Project " + project.id + " at " + mp.toString())
            try {
                require(!(project.redCapId != projectId || project.redCapUrl == redcapUrl)) {
                    message
                }
            } catch (exc: MalformedURLException) {
                throw IllegalRequestException(
                    message + " " + exc.message
                )
            }
        }

        @Throws(URISyntaxException::class, MalformedURLException::class)
        private fun getSubjectUrl(url: URL, projectName: String, recordId: Int): URL {
            val oldUri = url.toURI()
            val parameters = "projectName=$projectName&externalId=$recordId"
            var newQuery = oldUri.query
            if (newQuery == null) {
                newQuery = parameters
            } else {
                newQuery += parameters
            }

            val path = if (oldUri.path.endsWith('/')) oldUri.path.dropLast(1) else oldUri.path
            val newUri = URI(
                oldUri.scheme, oldUri.authority, path, newQuery, oldUri.fragment
            )
            LOGGER.info("URI = $newUri")
            return newUri.toURL()
        }
    }
}