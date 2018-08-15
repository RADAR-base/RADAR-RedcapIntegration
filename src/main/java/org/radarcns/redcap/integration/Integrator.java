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
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import okhttp3.FormBody.Builder;
import okhttp3.Request;
import org.radarcns.redcap.config.RedCapManager;
import org.radarcns.redcap.managementportal.MpClient;
import org.radarcns.redcap.managementportal.Project;
import org.radarcns.redcap.managementportal.Subject;
import org.radarcns.redcap.util.RedCapInput;
import org.radarcns.redcap.util.RedCapTrigger;
import org.radarcns.redcap.util.RedCapTrigger.InstrumentStatus;
import org.radarcns.redcap.util.RedCapUpdater;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Handler for updating Integrator Redcap form parameters. The input parameters are
 *      described by {@link IntegrationData}.
 * @see RedCapUpdater
 */
public class Integrator extends RedCapUpdater {

    private static final Logger Logger = LoggerFactory.getLogger(Integrator.class);

    private static final String SEPARATOR = "-";

    private MpClient mpClient;

    /**
     * Constructor.
     * @param trigger {@link RedCapTrigger} that has hit the service
     * @param mpClient {@link MpClient} used for making requests to Management Portal
     */
    public Integrator(RedCapTrigger trigger, MpClient mpClient) {
        super(trigger);
        this.mpClient = mpClient;
    }

    /**
     * Generates the {@link Set} of inputs that will be written in REDCap for finalising the
     *      integration between REDCap project and Management Portal project. Using a
     *      {@link MpClient}, the function retrieves the RADAR Subject Identifier and the Human
     *      Readable Identifier. In the end, the function forces the REDCap integration
     *      form /instrument status to {@link InstrumentStatus#COMPLETE}.
     * @return {@link Set} of inputs that have to be written in REDCap.
     */
    @Override
    protected Set<RedCapInput> getInput() {
        Subject subject = performSubjectUpdateOnMp(redCapInfo.getUrl(),
                redCapInfo.getProjectId(), getRecordId());

        Set<RedCapInput> set = new HashSet<>();

        set.add(new IntegrationData(getRecordId(), redCapInfo.getEnrolmentEvent(),
                IntegrationData.SUBJECT_ID_LABEL, subject.getSubjectId()));

        set.add(new IntegrationData(getRecordId(), redCapInfo.getEnrolmentEvent(),
                IntegrationData.HUMAN_READABLE_ID_LABEL, subject.getHumanReadableIdentifier()));

        set.add(new IntegrationData(getRecordId(), redCapInfo.getEnrolmentEvent(),
                RedCapManager.getStatusField(redCapInfo.getIntegrationForm()),
                Integer.toString(InstrumentStatus.COMPLETE.getStatus())));

        return set;
    }

    /**
     * Sets the HTML form parameters needed to update the REDCap integration form / instrument.
     *      It sets just the metadata not the real input, that is set by {@link RedCapUpdater}
     *      while generating the {@link Request}.
     * @param builder {@link okhttp3.FormBody.Builder} that has to be populated
     * @return {@link okhttp3.FormBody.Builder} required to finale the HTTP request.
     */
    @Override
    protected Builder setParameter(Builder builder) {
        return builder.add("content", "record")
                .add("format", "json")
                .add("type", "eav")
                .add("overwriteBehavior", "overwrite")
                .add("returnContent", "count")
                .add("returnFormat", "json");
    }


    /**
     * Performs update of the subject on Management portal from the information
     * sent from Redcap trigger. If subject does not exist, it creates a new one,
     * otherwise it just returns the subject.
     * TODO update the subject in case it exists
     * @see MpClient
     * @param redcapUrl the Redcap URL
     * @param projectId the Redcap Project ID
     * @param recordId the Redcap Record ID
     * @return {@link Subject} from the Management Portal
     */
    private Subject performSubjectUpdateOnMp(URL redcapUrl, Integer projectId,
                                     Integer recordId) {
        try {
            Project project = mpClient.getProject(redcapUrl, projectId);

            String radarWorkPackage = project.getWorkPackage().toUpperCase();
            String location = project.getLocation().toUpperCase();

            String humanReadableId = radarWorkPackage.concat(SEPARATOR).concat(
                    project.getId().toString()).concat(SEPARATOR).concat(location).concat(
                    SEPARATOR).concat(recordId.toString());

            Subject subject = mpClient.getSubject(redcapUrl, projectId, recordId);

            if (Objects.isNull(subject)) {
                Subject newSubject = mpClient.createSubject(redcapUrl, project, recordId, humanReadableId);

                Logger.info("Created RADAR subject: {}. Human readable identifier is: {}",
                        newSubject.getSubjectId(), humanReadableId);
                return newSubject;
            } else {
                Logger.info("Subject for Record Id: {} at {} is already available.", recordId,
                        redcapUrl);

                if (!humanReadableId.equals(subject.getHumanReadableIdentifier())) {
                    Logger.warn("Human Readable identifier for {} at {} does not reflect the "
                                    + "value stored in the Management Portal. {} is different from {}.",
                            recordId, redcapUrl.toString(), humanReadableId,
                            subject.getHumanReadableIdentifier());

                    //TODO
                    // update Subject in case the Human Readable Identifier does not match the
                    // expected one
                }

                return subject;
            }
        } catch (NullPointerException exc) {
            Logger.error("Project or Project attributes (Work Package, etc) cannot be null", exc);
            throw new IllegalStateException("Project or Project attributes (Work Package, etc) in MP cannot be null.", exc);
        } catch (Exception exc) {
            Logger.error(exc.getMessage(), exc);
            throw new IllegalStateException("Subject creation cannot be completed.", exc);
        }
    }
}
