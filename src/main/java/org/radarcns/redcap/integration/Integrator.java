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

import java.util.HashSet;
import java.util.Set;
import javax.servlet.ServletContext;
import okhttp3.FormBody.Builder;
import okhttp3.Request;
import org.radarcns.redcap.config.RedCapManager;
import org.radarcns.redcap.managementportal.MpClient;
import org.radarcns.redcap.util.RedCapInput;
import org.radarcns.redcap.util.RedCapTrigger;
import org.radarcns.redcap.util.RedCapTrigger.InstrumentStatus;
import org.radarcns.redcap.util.RedCapUpdater;

/** Handler for updating Integrator Redcap form parameters. The input parameters are
 *      described by {@link IntegrationData}.
 * @see RedCapUpdater
 */
public class Integrator extends RedCapUpdater {

    //private static final Logger LOGGER = LoggerFactory.getLogger(Integrator.class);

    /**
     * Constructor.
     * @param trigger {@link RedCapTrigger} that has hit the service
     * @param context {@link ServletContext} needed to extract shared variables
     */
    public Integrator(RedCapTrigger trigger, ServletContext context) {
        super(trigger, context);
    }

    /**
     * Generates the {@link Set} of inputs that will be written in REDCap for finalising the
     *      integration between REDCap project and Management Portal project. Using a
     *      {@link MpClient}, the function retrieves the RADAR Subject Identifier and the Human
     *      Readable Identifier. In the end, the function forces the REDCap integratio
     *      form /instrument status to {@link InstrumentStatus#COMPLETE}.
     * @return {@link Set} of inputs that have to be written in REDCap.
     */
    @Override
    protected Set<RedCapInput> getInput() {
        MpClient mpClient = new MpClient(redCapInfo.getUrl(),
                redCapInfo.getProjectId(), getRecordId(), context);

        Set<RedCapInput> set = new HashSet<>();

        set.add(new IntegrationData(getRecordId(), redCapInfo.getEnrolmentEvent(),
                IntegrationData.SUBJECT_ID_LABEL, mpClient.getRadarSubjectId()));

        set.add(new IntegrationData(getRecordId(), redCapInfo.getEnrolmentEvent(),
                IntegrationData.HUMAN_READABLE_ID_LABEL, mpClient.getHumanReadableId()));

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

}
