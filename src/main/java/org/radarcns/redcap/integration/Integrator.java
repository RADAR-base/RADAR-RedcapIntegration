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

import java.net.URL;
import java.util.*;

import org.radarcns.redcap.config.RedCapInfo;
import org.radarcns.redcap.config.RedCapManager;
import org.radarcns.redcap.managementportal.MpClient;
import org.radarcns.redcap.managementportal.Subject;
import org.radarcns.redcap.util.RedCapClient;
import org.radarcns.redcap.util.RedCapTrigger;

/** Handler for updating Integrator Redcap form parameters. The input parameters are
 *      described by {@link IntegrationData}.
 */
public class Integrator {
    private RedCapTrigger trigger;

    private RedCapInfo redCapInfo;

    private MpIntegrator mpIntegrator;

    private RedCapIntegator redCapIntegrator;

    /**
     * Constructor.
     * @param trigger {@link RedCapTrigger} that has hit the service
     * @param mpClient {@link MpClient} used for making requests to Management Portal
     */
    public Integrator(RedCapTrigger trigger, MpClient mpClient) {
        this.trigger = trigger;
        this.mpIntegrator = new MpIntegrator(mpClient);
        this.redCapInfo = RedCapManager.getInfo(trigger);
        this.redCapIntegrator = new RedCapIntegator(new RedCapClient(redCapInfo));
    }

    public boolean handleDataEntryTrigger(){
        Integer recordId = trigger.getRecord();
        Integer projectId = redCapInfo.getProjectId();
        String enrolmentEvent = redCapInfo.getEnrolmentEvent();
        String integrationFrom = redCapInfo.getIntegrationForm();
        URL url = redCapInfo.getUrl();
        List<String> attributeKeys = redCapInfo.getAttributeFieldNames();

        Map<String, String> attributes = redCapIntegrator.pullRecordAttributes(attributeKeys, recordId);
        String redcapSubjectId = redCapIntegrator.pullRecordSubjectId(recordId);
        Subject subject = mpIntegrator.performSubjectUpdateOnMp(url, projectId, recordId, attributes, redcapSubjectId);
        Subject.SubjectOperationStatus result = subject.getOprationStatus();
        if(result.equals(Subject.SubjectOperationStatus.CREATED) ){
            return redCapIntegrator.updateRedCapIntegrationForm(subject, recordId, enrolmentEvent, integrationFrom);
        }
        else {
            return (result.equals(Subject.SubjectOperationStatus.UPDATED) || result.equals(Subject.SubjectOperationStatus.OTHER))
                    && !result.equals(Subject.SubjectOperationStatus.FAILED);
        }
    }

}
