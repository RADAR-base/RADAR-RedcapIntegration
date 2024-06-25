package org.radarbase.redcap.integration

import org.radarbase.redcap.managementportal.MpClient
import org.radarbase.redcap.managementportal.Project
import org.radarbase.redcap.managementportal.Subject
import org.radarbase.redcap.webapp.exception.SubjectOperationException
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
 */
/** Handler for updating Management Portal subject. If the subject does not exists, it creates a
 * new subject, otherwise it updates the subject.
 * @see MpClient
 */
class MpIntegrator(private val mpClient: MpClient) {
    /**
     * Performs update of the subject on Management portal from the information
     * sent from Redcap trigger. If subject does not exist, it creates a new one,
     * otherwise it just returns the subject.
     * @see MpClient
     *
     * @param redcapUrl the Redcap URL
     * @param projectId the Redcap Project ID
     * @param recordId the Redcap Record ID
     * @return [Subject] from the Management Portal
     */
    fun performSubjectUpdateOnMp(
        redcapUrl: URL,
        projectId: Int,
        recordId: Int,
        attributes: Map<String, String>,
        redcapSubjectId: String?
    ): Subject {
        return try {
            val project = mpClient.getProject(redcapUrl, projectId)
            val workPackage = project.workPackage
            if (workPackage.isNullOrBlank()) {
                Logger.error("Work package in Management portal is null or empty")
                throw SubjectOperationException(
                    "Work Package in MP cannot be null or empty."
                )
            }
            if (project.location.isEmpty()) {
                Logger.error("Location is empty in management portal Project.")
                throw SubjectOperationException(
                    "Location for the project cannot be empty."
                )
            }

            val humanReadableId = createHumanReadableId(
                workPackage.toUpperCase(),
                project.id.toString(),
                project.location.toUpperCase(),
                recordId.toString()
            )

            subjectExistsUpdateElseCreate(
                redcapUrl,
                projectId,
                recordId,
                project,
                humanReadableId,
                attributes,
                redcapSubjectId
            )
        } catch (exc: Exception) {
            throw SubjectOperationException(
                "Subject creation cannot be completed.",
                exc
            )
        }
    }

    private fun subjectExistsUpdateElseCreate(
        redcapUrl: URL,
        projectId: Int,
        recordId: Int,
        project: Project,
        humanReadableId: String,
        attributes: Map<String, String>,
        redcapSubjectId: String?
    ): Subject {
        return try {
            val subject = mpClient.getSubject(redcapUrl, projectId, recordId)
            if (subject != null) {
                requireNotNull(redcapSubjectId) {
                    "Subject is not null in MP but no subject ID provided in redcap"
                }
                return if (subject.subjectId == redcapSubjectId) {
                    updateSubject(subject, attributes, humanReadableId)
                } else {
                    Logger.info(
                        "Subject already exists in MP and subject ids do not match!" +
                                " Integration failed."
                    )
                    subject.apply { operationStatus = Subject.SubjectOperationStatus.FAILED }
                }
            }
            createSubject(attributes, humanReadableId, redcapUrl, project, recordId)
        } catch (e: Exception) {
            throw SubjectOperationException("Subject creation cannot be completed.", e)
        }
    }

    private fun updateSubject(
        subject: Subject,
        attributes: Map<String, String>,
        humanReadableId: String
    ): Subject {
        Logger.info(
            "Subject, with Human readable identifier: {}, is already available, updating...",
            humanReadableId
        )
        val updatedAttributes = attributes + (HUMAN_READABLE_IDENTIFIER_KEY to humanReadableId)
        return if (subject.attributes != updatedAttributes) {
            subject.addAttributes(updatedAttributes)
            mpClient.updateSubject(subject)
                .apply { operationStatus = Subject.SubjectOperationStatus.UPDATED }
        } else {
            Logger.info("Existing attributes match new attributes! Not updating.")
            subject.apply { operationStatus = Subject.SubjectOperationStatus.NOOP }
        }
    }

    private fun createSubject(
        attributes: Map<String, String>,
        humanReadableId: String,
        redcapUrl: URL,
        project: Project,
        recordId: Int
    ): Subject {
        val subject =
            mpClient.createSubject(redcapUrl, project, recordId, humanReadableId, attributes)
        Logger.info(
            "Created RADAR subject: {}. Human readable identifier is: {}",
            subject.subjectId, humanReadableId
        )
        return subject.apply { operationStatus = Subject.SubjectOperationStatus.CREATED }
    }


    companion object {
        private val Logger = LoggerFactory.getLogger(MpIntegrator::class.java)
        private const val SEPARATOR = "-"
        private const val HUMAN_READABLE_IDENTIFIER_KEY = "Human-readable-identifier"
    }

    private fun createHumanReadableId(
        a: String,
        b: String,
        c: String,
        d: String
    ): String {
        return a + SEPARATOR + b + SEPARATOR + c + SEPARATOR + d
    }

}