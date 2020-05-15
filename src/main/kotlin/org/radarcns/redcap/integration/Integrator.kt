package org.radarcns.redcap.integration

import org.radarcns.redcap.config.RedCapInfo
import org.radarcns.redcap.config.RedCapManager
import org.radarcns.redcap.managementportal.MpClient
import org.radarcns.redcap.managementportal.Subject.SubjectOperationStatus.CREATED
import org.radarcns.redcap.managementportal.Subject.SubjectOperationStatus.FAILED
import org.radarcns.redcap.util.RedCapClient
import org.radarcns.redcap.util.RedCapTrigger
import org.radarcns.redcap.webapp.exception.IllegalRequestException
import org.radarcns.redcap.webapp.exception.SubjectOperationException
import org.slf4j.LoggerFactory

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
/** Handler for updating Integrator Redcap form parameters. The input parameters are
 * described by [IntegrationData].
 */
class Integrator(
    private val trigger: RedCapTrigger, private val mpClient: MpClient,
    private val redCapInfo: RedCapInfo = RedCapManager.getInfo(trigger),
    private val mpIntegrator: MpIntegrator = MpIntegrator(mpClient),
    private val redCapIntegrator: RedCapIntegator = RedCapIntegator(RedCapClient(redCapInfo))
) {

    fun handleDataEntryTrigger(): Boolean {
        val recordId = trigger.record
        val enrolmentEvent = redCapInfo.enrolmentEvent
        val integrationFrom = redCapInfo.integrationForm
        val keys = redCapInfo.attributes
            ?.map { a -> a.fieldName }
            ?.toMutableList()
            ?: mutableListOf()

        try {
            requireNotNull(recordId)
            requireNotNull(enrolmentEvent)
            requireNotNull(integrationFrom)
        } catch (exc: IllegalArgumentException) {
            throw IllegalRequestException("Some of the required values were missing.")
        }

        keys.add(IntegrationData.SUBJECT_ID_LABEL)
        logger.info("Attribute Keys: {}", keys.toTypedArray())
        val result = redCapIntegrator.pullFieldsFromRedcap(keys, recordId)

        val subject =
            mpIntegrator.performSubjectUpdateOnMp(
                redCapInfo.url,
                redCapInfo.projectId,
                recordId,
                result,
                result.remove(IntegrationData.SUBJECT_ID_LABEL)
            )
        return when (subject.operationStatus) {
            CREATED -> redCapIntegrator.updateRedCapIntegrationForm(
                subject,
                recordId,
                enrolmentEvent,
                integrationFrom
            )

            FAILED -> throw SubjectOperationException("Operation on Subject in MP failed.")
            else -> false
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(Integrator::class.java)
    }
}