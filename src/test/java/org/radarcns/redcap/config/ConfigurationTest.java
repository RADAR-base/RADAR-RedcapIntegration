package org.radarcns.redcap.config;

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
import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import org.junit.Test;
import org.radarcns.config.YamlConfigLoader;

public class ConfigurationTest {

    @Test
    public void loadConfigTest() throws IOException {
        Configuration config = new YamlConfigLoader().load(new File(
                ConfigurationTest.class.getClassLoader().getResource("config.yml").getFile()),
                Configuration.class);

        assertEquals("1.0", config.getVersion());
        assertEquals("2017-08-29", config.getReleased());
        assertEquals("todo", config.getOauthClientId());
        assertEquals("todo", config.getOauthClientSecret());
        assertEquals(new URL("https://localhost"), config.getManagementPortalUrl());
        assertEquals("/token", config.getTokenEndpoint());
        assertEquals("/project", config.getProjectEndpoint());
        assertEquals("/subject", config.getSubjectEndpoint());

        assertEquals(Collections.singleton(new RedCapInfo(
                new URL("https://localhost/"),
                0, "enrolment", "radar_enrolment",
                "1234567890")), config.getRedCapInstances());
    }

}
