package org.radarcns.redcap.config

import org.junit.Assert
import org.junit.Test
import java.io.File
import java.io.IOException
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
class ConfigurationTest {
    @Test
    @Throws(IOException::class)
    fun loadConfigTest() {
        val filePath = ConfigurationTest::class.java.classLoader
            .getResource("config.yml")?.file

        if (!Properties.checkFileExist(filePath)) {
            throw IllegalStateException("File does not exist")
        }

        val configuration = Properties.loadConfig(File(filePath!!))
        Assert.assertEquals("1.0", configuration.version)
        Assert.assertEquals("2017-08-29", configuration.released)
        Assert.assertEquals("todo", configuration.oauthClientId)
        Assert.assertEquals("todo", configuration.oauthClientSecret)
        Assert.assertEquals(URL("https://localhost"), configuration.managementPortalUrl)
        Assert.assertEquals("/token", configuration.tokenEndpoint)
        Assert.assertEquals("/project", configuration.projectEndpoint)
        Assert.assertEquals("/subject", configuration.subjectEndpoint)
        Assert.assertEquals(
            listOf("abc", "xyz"), configuration.projects.first().redCapInfo.attributes?.map { a ->
                a.fieldName
            }
        )
        val redCapInfo = RedCapInfo(
            URL("https://localhost/"),
            0, "enrolment", "radar_enrolment",
            "1234567890", emptySet()
        )
        val mpInfo = ManagementPortalInfo("project-0")
        Assert.assertEquals(setOf(ProjectInfo(redCapInfo, mpInfo)), configuration.projects)
    }
}