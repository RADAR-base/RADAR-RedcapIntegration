package org.radarcns.redcap.integration

import org.junit.Assert
import org.junit.Assert.assertFalse
import org.junit.Test
import org.radarcns.redcap.managementportal.Subject
import org.radarcns.redcap.util.IntegrationUtils
import org.radarcns.redcap.util.RedCapTrigger
import java.net.URL

class IntegratorTest {

    @Test
    fun inputTest() {
        val trigger = RedCapTrigger(IntegrationUtils.TRIGGER_BODY)
        val integrator = Integrator(trigger, IntegrationUtils.mpClient)

        // The result will be false since there is no actual redcap instance and hence the form
        // cannot be updated. But even then, no exception should be thrown.
        val result = integrator.handleDataEntryTrigger()
        assertFalse(result)

        // We can check if corresponding subject is created in MP too.
        val subject: Subject? =
            IntegrationUtils.mpClient.getSubject(
                URL(IntegrationUtils.REDCAP_URL),
                IntegrationUtils.REDCAP_PROJECT_ID,
                IntegrationUtils.REDCAP_RECORD_ID_2
            )
        Assert.assertNotNull(subject)
        Assert.assertEquals(
            Integer.valueOf(IntegrationUtils.REDCAP_RECORD_ID_2),
            subject?.externalId
        )
        Assert.assertEquals(
            "${IntegrationUtils.WORK_PACKAGE}-${IntegrationUtils.MP_PROJECT_ID}-" +
                    "${IntegrationUtils.MP_PROJECT_LOCATION}-" +
                    "${IntegrationUtils.REDCAP_RECORD_ID_2}", subject?.humanReadableIdentifier
        )
        Assert.assertEquals("radar", subject?.project?.projectName)
    }
}