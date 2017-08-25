package org.radarcns.redcap.enrolment;

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
import okhttp3.FormBody.Builder;
import org.radarcns.redcap.util.IdManager;
import org.radarcns.redcap.util.RedCapInput;
import org.radarcns.redcap.util.RedCapTrigger;
import org.radarcns.redcap.util.RedCapTrigger.InstrumentStatus;
import org.radarcns.redcap.util.RedCapUpdater;

/** Handler for updating RadarEnrolment Redcap form parameters. The input parameters are
 *      described by {@link EnrolmentInput}.
 * @see RedCapUpdater
 */
public class RadarEnrolment extends RedCapUpdater {

    public RadarEnrolment(RedCapTrigger trigger) {
        super(trigger);
    }

    @Override
    protected List<RedCapInput> getInput() {
        IdManager ids = new IdManager(trigger);

        List<RedCapInput> list = new LinkedList<>();

        list.add(new EnrolmentInput(trigger.getRecord(), trigger.getRedcapEventName(),
                EnrolmentInput.SUBJECT_ID_LABEL, ids.getRadarId()));

        list.add(new EnrolmentInput(trigger.getRecord(), trigger.getRedcapEventName(),
                EnrolmentInput.HUMAN_READABLE_ID_LABEL, ids.getHumanReadableId()));

        list.add(new EnrolmentInput(trigger.getRecord(), trigger.getRedcapEventName(),
                trigger.getInstrumentStatusField(),
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
