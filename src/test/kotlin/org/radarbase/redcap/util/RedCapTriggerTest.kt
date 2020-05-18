package org.radarbase.redcap.util

import junit.framework.TestCase
import org.junit.Test
import org.radarbase.redcap.util.RedCapTrigger.InstrumentStatus
import java.net.MalformedURLException
import java.net.URL

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
class RedCapTriggerTest {
    @Test
    @Throws(MalformedURLException::class)
    fun testConstructor() {
        val input = ("redcap_url=https%3A%2F%2Fredcap.com%2F&"
                + "project_url=https://redcap.com/redcap_v6.10.1/index.php?pid=33&"
                + "project_id=33&"
                + "username=test&"
                + "record=1&"
                + "redcap_event_name=baseline_assessmen_arm_1&"
                + "instrument=radar_enrolment&"
                + "radar_enrolment_complete=0")
        val trigger = RedCapTrigger(input)
        TestCase.assertEquals(33.0, trigger.projectId!!.toDouble(), 0.0)
        TestCase.assertEquals("test", trigger.username)
        TestCase.assertEquals("radar_enrolment", trigger.instrument)
        TestCase.assertEquals(1.0, trigger.record!!.toDouble(), 0.0)
        TestCase.assertEquals("baseline_assessmen_arm_1", trigger.redcapEventName)
        TestCase.assertNull(trigger.redcapDataAccessGroup)
        TestCase.assertEquals(
            InstrumentStatus.INCOMPLETE.status.toDouble(), trigger.status!!.status.toDouble(),
            0.0
        )
        TestCase.assertEquals(URL("https://redcap.com/redcap_v6.10.1/"), trigger.redcapUrl)
        TestCase.assertEquals(
            URL("https://redcap.com/redcap_v6.10.1/index.php?pid=33"),
            trigger.projectUrl
        )
    }
}