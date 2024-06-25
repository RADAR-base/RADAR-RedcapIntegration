package org.radarbase.redcap.integration

import org.radarbase.redcap.config.RedCapInfo
import org.radarbase.redcap.config.RedCapManager
import org.radarbase.redcap.managementportal.MpClient
import org.radarbase.redcap.managementportal.Subject.SubjectOperationStatus.*
import org.radarbase.redcap.util.RedCapClient
import org.radarbase.redcap.util.RedCapTrigger
import org.radarbase.redcap.webapp.exception.IllegalRequestException
import org.radarbase.redcap.webapp.exception.RedcapOperationException
import org.radarbase.redcap.webapp.exception.SubjectOperationException
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
    private val trigger: RedCapTrigger,
    private val mpClient: MpClient,
    private val redCapInfo: RedCapInfo = RedCapManager.getInfo(trigger),
    private val mpIntegrator: MpIntegrator =
        MpIntegrator(
            mpClient,
        ),
    private val redCapIntegrator: RedCapIntegator =
        RedCapIntegator(
            RedCapClient(redCapInfo),
        ),
) {
    fun handleDataEntryTrigger(): Boolean {
        val recordId = trigger.record
        val enrolmentEvent = redCapInfo.enrolmentEvent
        val integrationFrom = redCapInfo.integrationForm
        val keys = redCapInfo.attributes?.map { a -> a.fieldName } ?: emptyList()
        var attributes = emptyMap<String, String>()
        var redcapSubjectId = ""

        try {
            requireNotNull(recordId)
            requireNotNull(enrolmentEvent)
            requireNotNull(integrationFrom)
        } catch (exc: IllegalArgumentException) {
            throw IllegalRequestException("Some of the required values were missing.")
        }

        logger.info("Attribute Keys: {}", keys.toTypedArray())
        try {
            attributes = redCapIntegrator.pullFieldsFromRedcap(keys, recordId)
            redcapSubjectId = redCapIntegrator.pullFieldsFromRedcap(listOf(SUJBECT_ID_KEY), recordId, enrolmentEvent).getOrDefault(SUJBECT_ID_KEY, "")
        } catch (exc: RedcapOperationException) {
            logger.warn("Error getting fields from Redcap. Using null as result..", exc)
        }

        val subject =
            mpIntegrator.performSubjectUpdateOnMp(
                redCapInfo.url,
                redCapInfo.projectId,
                recordId,
                attributes,
                redcapSubjectId,
            )

        return when (subject.operationStatus) {
            CREATED ->
                redCapIntegrator.updateRedCapIntegrationForm(
                    subject,
                    recordId,
                    enrolmentEvent,
                    integrationFrom,
                )

            // We send true here too since the integration was successful even though we did not
            // update the redcap form as it wasn't required. Later, we may update a field in
            // redcap with attributes from MP in the UPDATED status
            UPDATED, NOOP -> true

            FAILED -> throw SubjectOperationException("Operation on Subject in MP failed.")

            else -> false
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(Integrator::class.java)
        private val SUJBECT_ID_KEY = IntegrationData.SUBJECT_ID_LABEL
    }
}