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


import org.radarcns.redcap.managementportal.MpClient;
import org.radarcns.redcap.managementportal.Project;
import org.radarcns.redcap.managementportal.Subject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.Map;

/** Handler for updating Integrator Redcap form parameters. The input parameters are
 *      described by {@link IntegrationData}.
 * @see MpClient
 */
public class MpIntegrator {

    private static final Logger Logger = LoggerFactory.getLogger(MpIntegrator.class);

    private static final String SEPARATOR = "-";

    private static final String HUMAN_READABLE_IDENTIFIER_KEY = "Human-readable-identifier";

    private MpClient mpClient;

    public MpIntegrator(MpClient mpClient) {
        this.mpClient = mpClient;
    }

    /**
     * Performs update of the subject on Management portal from the information
     * sent from Redcap trigger. If subject does not exist, it creates a new one,
     * otherwise it just returns the subject.
     * @see MpClient
     * @param redcapUrl the Redcap URL
     * @param projectId the Redcap Project ID
     * @param recordId the Redcap Record ID
     * @return {@link Subject} from the Management Portal
     */
    public Subject performSubjectUpdateOnMp(URL redcapUrl, Integer projectId,
                                             Integer recordId, Map<String, String> attributes, String redcapSubjectId) {
        try {
            Project project = mpClient.getProject(redcapUrl, projectId);
            String radarWorkPackage = project.getWorkPackage().toUpperCase();
            String location = project.getLocation().toUpperCase();
            String humanReadableId = createHumanReadableId(radarWorkPackage, project.getId().toString(), location, recordId.toString());

            Subject subject = subjectExistsUpdateElseCreate(redcapUrl, projectId, recordId, project, humanReadableId, attributes, redcapSubjectId);
            return subject;

        } catch (NullPointerException exc) {
            Logger.error("Project or Project attributes (Work Package, etc) cannot be null", exc);
            throw new IllegalStateException("Project or Project attributes (Work Package, etc) in MP cannot be null.", exc);
        } catch (Exception exc) {
            Logger.error(exc.getMessage(), exc);
            throw new IllegalStateException("Subject creation cannot be completed.", exc);
        }
    }

    private Subject subjectExistsUpdateElseCreate(URL redcapUrl, Integer projectId, Integer recordId,
                                                  Project project, String humanReadableId, Map<String, String> attributes, String redcapSubjectId){
        try {
            Subject subject = mpClient.getSubject(redcapUrl, projectId, recordId);
            if(subject != null) {
                if(subject.getSubjectId().equals(redcapSubjectId)) {
                    return updateSubject(subject, attributes, humanReadableId);
                }
                else {
                    Logger.info("Subject already exists in MP and subject ids do not match! Integration failed.");
                    subject.setOprationStatus(Subject.SubjectOperationStatus.FAILED);
                    return subject;
                }
            }
            else {
                return createSubject(attributes, humanReadableId, redcapUrl, project, recordId);
            }
        } catch (Exception e) {
            throw new IllegalStateException("Subject creation cannot be completed.", e);
        }
    }

    private Subject updateSubject(Subject subject, Map<String, String> attributes, String humanReadableId){
        Logger.info("Subject, with Human readable identifier: {}, is already available, updating...", humanReadableId);
        attributes.put(HUMAN_READABLE_IDENTIFIER_KEY, humanReadableId);
        Map<String, String> existingAttributes = subject.getAttributes();
        if (!existingAttributes.equals(attributes)) {
            subject.setAttributes(attributes);
            Subject updatedSubject = mpClient.updateSubject(subject);
            updatedSubject.setOprationStatus(Subject.SubjectOperationStatus.UPDATED);
            return updatedSubject;
        } else {
            subject.setOprationStatus(Subject.SubjectOperationStatus.NOOP);
            Logger.info("Existing attributes match new attributes! Not updating.");
            return subject;
        }
    }

    private Subject createSubject(Map<String, String> attributes, String humanReadableId, URL redcapUrl, Project project, Integer recordId){
        Subject subject = mpClient.createSubject(redcapUrl, project, recordId, humanReadableId, attributes);
        Logger.info("Created RADAR subject: {}. Human readable identifier is: {}",
                subject.getSubjectId(), humanReadableId);
        subject.setOprationStatus(Subject.SubjectOperationStatus.CREATED);
        return subject;
    }

    private String createHumanReadableId(String a, String b, String c, String d){
        String humanReadableId = a.concat(SEPARATOR).concat(
               b).concat(SEPARATOR).concat(c).concat(
                SEPARATOR).concat(d);
        return humanReadableId;
    }

}
