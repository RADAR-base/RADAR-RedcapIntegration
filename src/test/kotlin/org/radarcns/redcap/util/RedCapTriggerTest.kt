package org.radarcns.redcap.util;

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

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNull;

import java.net.MalformedURLException;
import java.net.URL;
import org.junit.Test;
import org.radarcns.redcap.util.RedCapTrigger.InstrumentStatus;

public class RedCapTriggerTest {

    @Test
    public void testConstructor() throws MalformedURLException {
        String input = "redcap_url=https%3A%2F%2Fredcap.com%2F&"
                + "project_url=https://redcap.com/redcap_v6.10.1/index.php?pid=33&"
                + "project_id=33&"
                + "username=test&"
                + "record=1&"
                + "redcap_event_name=baseline_assessmen_arm_1&"
                + "instrument=radar_enrolment&"
                + "radar_enrolment_complete=0";

        RedCapTrigger trigger = new RedCapTrigger(input);

        assertEquals(33, trigger.getProjectId(), 0.0);
        assertEquals("test", trigger.getUsername());
        assertEquals("radar_enrolment", trigger.getInstrument());
        assertEquals(1, trigger.getRecord(), 0.0);
        assertEquals("baseline_assessmen_arm_1", trigger.getRedcapEventName());
        assertNull(trigger.getRedcapDataAccessGroup());
        assertEquals(InstrumentStatus.INCOMPLETE.getStatus(), trigger.getStatus().getStatus(),
                0.0);
        assertEquals(new URL("https://redcap.com/redcap_v6.10.1/"), trigger.getRedcapUrl());
        assertEquals(new URL("https://redcap.com/redcap_v6.10.1/index.php?pid=33"),
                trigger.getProjectUrl());
    }

}
