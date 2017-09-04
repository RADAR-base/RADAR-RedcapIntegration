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

import java.util.LinkedList;
import java.util.List;
import javax.servlet.ServletContext;
import okhttp3.FormBody.Builder;
import okhttp3.OkHttpClient;
import org.radarcns.redcap.config.RedCapManager;
import org.radarcns.redcap.listener.HttpClientListener;
import org.radarcns.redcap.managementportal.ManagementPortalClient;
import org.radarcns.redcap.util.RedCapInput;
import org.radarcns.redcap.util.RedCapTrigger;
import org.radarcns.redcap.util.RedCapTrigger.InstrumentStatus;
import org.radarcns.redcap.util.RedCapUpdater;

/** Handler for updating Integrator Redcap form parameters. The input parameters are
 *      described by {@link IntegrationData}.
 * @see RedCapUpdater
 */
public class Integrator extends RedCapUpdater {

    public Integrator(RedCapTrigger trigger, ServletContext context) {
        super(trigger, context);
    }

    @Override
    protected List<RedCapInput> getInput() {
        ManagementPortalClient mpClient = new ManagementPortalClient(redCapInfo.getUrl(),
                redCapInfo.getProjectId(), getRecordId(), context);

        List<RedCapInput> list = new LinkedList<>();

        list.add(new IntegrationData(getRecordId(), redCapInfo.getEnrolmentEvent(),
                IntegrationData.SUBJECT_ID_LABEL, mpClient.getRadarSubjectId()));

        list.add(new IntegrationData(getRecordId(), redCapInfo.getEnrolmentEvent(),
                IntegrationData.HUMAN_READABLE_ID_LABEL, mpClient.getHumanReadableId()));

        list.add(new IntegrationData(getRecordId(), redCapInfo.getEnrolmentEvent(),
                RedCapManager.getStatusField(redCapInfo),
                Integer.toString(InstrumentStatus.COMPLETE.getStatus())));

        return list;
    }

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
