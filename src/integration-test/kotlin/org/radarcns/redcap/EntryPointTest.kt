package org.radarcns.redcap

import org.junit.Assert
import org.junit.Test
import org.radarcns.redcap.managementportal.Subject
import org.radarcns.redcap.util.IntegrationClient.makeTriggerRequest
import org.radarcns.redcap.util.IntegrationUtils.MP_PROJECT_ID
import org.radarcns.redcap.util.IntegrationUtils.MP_PROJECT_LOCATION
import org.radarcns.redcap.util.IntegrationUtils.REDCAP_PROJECT_ID
import org.radarcns.redcap.util.IntegrationUtils.REDCAP_RECORD_ID_2
import org.radarcns.redcap.util.IntegrationUtils.REDCAP_URL
import org.radarcns.redcap.util.IntegrationUtils.TRIGGER_BODY
import org.radarcns.redcap.util.IntegrationUtils.WORK_PACKAGE
import org.radarcns.redcap.util.IntegrationUtils.mpClient
import java.io.IOException
import java.net.URISyntaxException
import java.net.URL
import javax.ws.rs.core.MediaType

class EntryPointTest {
    @Test
    @Throws(IOException::class, URISyntaxException::class)
    fun triggerTest() {
        val response = makeTriggerRequest(
            TRIGGER_BODY,
            MediaType.TEXT_PLAIN
        )
        Assert.assertNotNull(response)
        // This is because the the app fails to update redcap form as there is no actual redcap instance.
        // So the request times out with an exception
        Assert.assertEquals(500, response.code().toLong())

        // But we can verify if the corresponding subject creation in MP works fine.
        val subject: Subject? =
            mpClient.getSubject(URL(REDCAP_URL), REDCAP_PROJECT_ID, REDCAP_RECORD_ID_2)
        Assert.assertNotNull(subject)
        Assert.assertEquals(Integer.valueOf(REDCAP_RECORD_ID_2), subject?.externalId)
        Assert.assertEquals(
            "$WORK_PACKAGE-$MP_PROJECT_ID-$MP_PROJECT_LOCATION-$REDCAP_RECORD_ID_2",
            subject?.humanReadableIdentifier
        )
        Assert.assertEquals("radar", subject?.project?.projectName)
    }
}