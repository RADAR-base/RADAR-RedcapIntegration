package org.radarcns.redcap.integration

import org.radarcns.redcap.config.RedCapManager
import org.radarcns.redcap.managementportal.Subject
import org.radarcns.redcap.util.AttributeFieldParser
import org.radarcns.redcap.util.RedCapClient
import org.radarcns.redcap.util.RedCapInput
import org.radarcns.redcap.util.RedCapTrigger


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
 * @see RedCapClient
 */
class RedCapIntegator(private val redCapClient: RedCapClient) {
    fun updateRedCapIntegrationForm(
        subject: Subject,
        recordId: Int,
        enrolmentEvent: String,
        integrationForm: String
    ): Boolean = redCapClient.updateForm(
        getFormDataToUpdate(subject, recordId, enrolmentEvent, integrationForm),
        recordId
    )

    /**
     * Generates the [Set] of inputs that will be written in REDCap for finalising the
     * integration between REDCap project and Management Portal project. Using a
     * [org.radarcns.redcap.managementportal.MpClient], the function retrieves the RADAR Subject
     * Identifier and the Human
     * Readable Identifier. In the end, the function forces the REDCap integration
     * form /instrument status to [RedCapTrigger.InstrumentStatus.COMPLETE].
     * @return [Set] of inputs that have to be written in REDCap.
     */
    fun getFormDataToUpdate(
        subject: Subject,
        recordId: Int,
        enrolmentEvent: String,
        integrationForm: String
    ): Set<RedCapInput> = mutableSetOf<RedCapInput>().apply {
        add(
            IntegrationData(
                recordId, enrolmentEvent,
                IntegrationData.SUBJECT_ID_LABEL, subject.subjectId
            )
        )
        add(
            IntegrationData(
                recordId, enrolmentEvent,
                IntegrationData.HUMAN_READABLE_ID_LABEL, subject.humanReadableIdentifier!!
            )
        )
        add(
            IntegrationData(
                recordId, enrolmentEvent, RedCapManager.getStatusField(integrationForm),
                RedCapTrigger.InstrumentStatus.COMPLETE.status.toString()
            )
        )
    }

    fun pullFieldsFromRedcap(fields: List<String>, recordId: Int) =
        redCapClient.fetchFormDataForId(fields, recordId)
            .filter { parser.canBeParsed(it.value) }
            .mapValues { parser.parseField(it.value) }
            .toMutableMap()

    companion object {
        val parser = AttributeFieldParser()
    }
}