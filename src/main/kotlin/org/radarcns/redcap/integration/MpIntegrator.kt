package org.radarcns.redcap.integration

import org.radarcns.redcap.managementportal.MpClient
import org.radarcns.redcap.managementportal.Project
import org.radarcns.redcap.managementportal.Subject
import org.slf4j.LoggerFactory
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
 */ /** Handler for updating Integrator Redcap form parameters. The input parameters are
 * described by [IntegrationData].
 * @see MpClient
 */
class MpIntegrator(private val mpClient: MpClient) {
    /**
     * Performs update of the subject on Management portal from the information
     * sent from Redcap trigger. If subject does not exist, it creates a new one,
     * otherwise it just returns the subject.
     * TODO update the subject in case it exists
     * @see MpClient
     *
     * @param redcapUrl the Redcap URL
     * @param projectId the Redcap Project ID
     * @param recordId the Redcap Record ID
     * @return [Subject] from the Management Portal
     */
    fun performSubjectUpdateOnMp(
        redcapUrl: URL, projectId: Int,
        recordId: Int, attributes: MutableMap<String, String>
    ): Subject {
        return try {
            val project = mpClient.getProject(redcapUrl, projectId)
            val radarWorkPackage = project.workPackage!!.toUpperCase()
            val location = project.location.toUpperCase()
            val humanReadableId = createHumanReadableId(
                radarWorkPackage,
                project.id.toString(),
                location,
                recordId.toString()
            )
            subjectExistsUpdateElseCreate(
                redcapUrl, projectId, recordId, project, humanReadableId, attributes
            )
        } catch (exc: NullPointerException) {
            throw IllegalStateException(
                "Project or Project attributes (Work Package, etc) in MP cannot be null.",
                exc
            )
        } catch (exc: Exception) {
            throw IllegalStateException("Subject creation cannot be completed.", exc)
        }
    }

    private fun subjectExistsUpdateElseCreate(
        redcapUrl: URL,
        projectId: Int,
        recordId: Int,
        project: Project,
        humanReadableId: String,
        attributes: MutableMap<String, String>
    ): Subject {
        return try {
            var subject =
                mpClient.getSubject(redcapUrl, projectId, recordId)
            if (subject != null) {
                Logger.info(
                    "Subject for Record Id: {} at {} is already available, updating...", recordId,
                    redcapUrl
                )
                attributes[HUMAN_READABLE_IDENTIFIER_KEY] = humanReadableId
                subject.addAttributes(attributes)
                subject = mpClient.updateSubject(subject)
            } else {
                subject = mpClient.createSubject(
                    redcapUrl,
                    project,
                    recordId,
                    humanReadableId,
                    attributes
                )
                Logger.info(
                    "Created RADAR subject: {}. Human readable identifier is: {}",
                    subject.subjectId, humanReadableId
                )
            }
            subject
        } catch (e: Exception) {
            throw IllegalStateException("Subject creation cannot be completed.", e)
        }
    }

    private fun createHumanReadableId(
        a: String,
        b: String,
        c: String,
        d: String
    ): String {
        return a + SEPARATOR +
                b + SEPARATOR + c +
                SEPARATOR + d
    }

    companion object {
        private val Logger = LoggerFactory.getLogger(MpIntegrator::class.java)
        private const val SEPARATOR = "-"
        private const val HUMAN_READABLE_IDENTIFIER_KEY = "Human-readable-identifier"
    }

}