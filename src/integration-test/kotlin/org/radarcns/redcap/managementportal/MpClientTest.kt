package org.radarcns.redcap.managementportal

import org.junit.Assert
import org.junit.Before
import org.junit.FixMethodOrder
import org.junit.Test
import org.junit.runners.MethodSorters
import org.radarcns.exception.TokenException
import org.radarcns.redcap.util.IntegrationUtils
import org.radarcns.redcap.util.IntegrationUtils.mpClient
import org.radarcns.redcap.webapp.exception.SubjectOperationException
import java.io.IOException
import java.net.URISyntaxException
import java.net.URL
import java.util.*

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class MpClientTest {
    private var project: Project? = null
    private val testAttributes: Map<String, String> =
        HashMap()

    @Before
    @Throws(IOException::class, TokenException::class)
    fun init() {
        IntegrationUtils.updateProjectAttributes()
    }

    @Throws(IOException::class)
    @Test
    fun projectTest() {
        project = mpClient.getProject(
            URL(IntegrationUtils.REDCAP_URL),
            IntegrationUtils.REDCAP_PROJECT_ID
        )
        Assert.assertEquals("radar", project!!.projectName)
    }

    @Test
    @Throws(IOException::class, URISyntaxException::class)
    fun createSubjectTest1() {
        if (project == null) {
            projectTest()
        }
        mpClient.createSubject(
            URL(IntegrationUtils.REDCAP_URL), project!!, IntegrationUtils.REDCAP_RECORD_ID_1,
            IntegrationUtils.WORK_PACKAGE + IntegrationUtils.REDCAP_RECORD_ID_1, testAttributes
        )
        val subject =
            mpClient.getSubject(
                URL(IntegrationUtils.REDCAP_URL), IntegrationUtils.REDCAP_PROJECT_ID,
                IntegrationUtils.REDCAP_RECORD_ID_1
            )
        Assert.assertNotNull(subject)
        Assert.assertEquals(
            Integer.valueOf(IntegrationUtils.REDCAP_RECORD_ID_1),
            subject!!.externalId
        )
        Assert.assertEquals(
            IntegrationUtils.WORK_PACKAGE + IntegrationUtils.REDCAP_RECORD_ID_1,
            subject.humanReadableIdentifier
        )
    }

    @Test(expected = SubjectOperationException::class)
    @Throws(IOException::class)
    fun createSubjectTest2() {
        if (project == null) {
            projectTest()
        }
        // This should throw exception since subject already exists
        mpClient.createSubject(
            URL(IntegrationUtils.REDCAP_URL), project!!, IntegrationUtils.REDCAP_RECORD_ID_1,
            IntegrationUtils.WORK_PACKAGE + IntegrationUtils.REDCAP_RECORD_ID_1, testAttributes
        )
    }
}