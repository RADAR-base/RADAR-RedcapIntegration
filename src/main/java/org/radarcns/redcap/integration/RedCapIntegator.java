package org.radarcns.redcap.integration;

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

import org.radarcns.redcap.config.RedCapManager;
import org.radarcns.redcap.managementportal.MpClient;
import org.radarcns.redcap.managementportal.Subject;
import org.radarcns.redcap.util.AttributeFieldParser;
import org.radarcns.redcap.util.RedCapClient;
import org.radarcns.redcap.util.RedCapInput;
import org.radarcns.redcap.util.RedCapTrigger;

import java.util.*;

/** Handler for updating Integrator Redcap form parameters. The input parameters are
 *      described by {@link IntegrationData}.
 * @see RedCapClient
 */
public class RedCapIntegator {
    private RedCapClient redCapClient;

    public RedCapIntegator(RedCapClient redCapClient) {
        this.redCapClient = redCapClient;
    }

    public boolean updateRedCapIntegrationForm(Subject subject, Integer recordId, String enrolmentEvent, String integrationForm){
        Set<RedCapInput> data = getFormDataToUpdate(subject, recordId, enrolmentEvent, integrationForm);
        return redCapClient.updateForm(data, recordId);
    }

    /**
     * Generates the {@link Set} of inputs that will be written in REDCap for finalising the
     *      integration between REDCap project and Management Portal project. Using a
     *      {@link MpClient}, the function retrieves the RADAR Subject Identifier and the Human
     *      Readable Identifier. In the end, the function forces the REDCap integration
     *      form /instrument status to {@link RedCapTrigger.InstrumentStatus#COMPLETE}.
     * @return {@link Set} of inputs that have to be written in REDCap.
     */
    public Set<RedCapInput> getFormDataToUpdate(Subject subject, Integer recordId, String enrolmentEvent, String integrationForm) {
        Set<RedCapInput> set = new HashSet<>();
        set.add(new IntegrationData(recordId, enrolmentEvent,
                IntegrationData.SUBJECT_ID_LABEL, subject.getSubjectId()));
        set.add(new IntegrationData(recordId, enrolmentEvent,
                IntegrationData.HUMAN_READABLE_ID_LABEL, subject.getHumanReadableIdentifier()));
        set.add(new IntegrationData(recordId, enrolmentEvent,
                RedCapManager.getStatusField(integrationForm),
                Integer.toString(RedCapTrigger.InstrumentStatus.COMPLETE.getStatus())));
        return set;
    }

    public Map<String, String> pullRecordAttributes(List<String> attributes, Integer recordId){
            Map<String, String> fetchedAttributes = new HashMap<>();
            AttributeFieldParser parser = new AttributeFieldParser();
            Map<String, String> fieldData = redCapClient.fetchFormDataForId(attributes, recordId);
            for (Map.Entry<String, String> entry : fieldData.entrySet())
                fetchedAttributes.put(entry.getKey(), parser.parseField(entry.getValue()));
            return fetchedAttributes;
    }

    public String pullRecordSubjectId(Integer recordId) {
        List<String> fields = new ArrayList<>();
        fields.add(IntegrationData.SUBJECT_ID_LABEL);
        Map<String, String> fieldData = redCapClient.fetchFormDataForId(fields, recordId);
        return fieldData.get(IntegrationData.SUBJECT_ID_LABEL);
    }

    public Map<String, String> pullRecordSubjectIdAndAttributes(List<String> attributes, Integer recordId) {
        // Add subject_id field to attribute fields list
        attributes.add(IntegrationData.SUBJECT_ID_LABEL);
        Map<String, String> data = new HashMap<>();
        AttributeFieldParser parser = new AttributeFieldParser();
        Map<String, String> fieldData = redCapClient.fetchFormDataForId(attributes, recordId);
        for (Map.Entry<String, String> entry : fieldData.entrySet())
            data.put(entry.getKey(), parser.parseField(entry.getValue()));
        return data;
    }

}
