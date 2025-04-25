package org.radarbase.redcap.managementportal

import org.junit.Assert
import org.junit.Before
import org.junit.FixMethodOrder
import org.junit.Test
import org.junit.runners.MethodSorters
import org.radarbase.exception.TokenException
import org.radarbase.redcap.util.IntegrationUtils
import org.radarbase.redcap.util.IntegrationUtils.mpClient
import java.io.IOException
import java.net.URISyntaxException
import java.net.URL

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class MpClientTest {
    private var project: Project? = null
    private val emptyAttributes: Map<String, String> = HashMap()
    private val attributes: Map<String, String> = mapOf("group" to "test")

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
            IntegrationUtils.WORK_PACKAGE + IntegrationUtils.REDCAP_RECORD_ID_1, emptyAttributes
        )
        val subject =
            mpClient.getSubject(
                URL(IntegrationUtils.REDCAP_URL), IntegrationUtils.REDCAP_PROJECT_ID,
                IntegrationUtils.REDCAP_RECORD_ID_1
            )
        Assert.assertNotNull(subject)
        Assert.assertEquals(
            IntegrationUtils.REDCAP_RECORD_ID_1.toString(),
            subject!!.externalId
        )
        Assert.assertEquals(
            IntegrationUtils.WORK_PACKAGE + IntegrationUtils.REDCAP_RECORD_ID_1,
            subject.humanReadableIdentifier
        )
    }

    @Test(expected = IOException::class)
    @Throws(IOException::class)
    fun createSubjectTest2() {
        if (project == null) {
            projectTest()
        }
        // This should throw exception since subject already exists
        mpClient.createSubject(
            URL(IntegrationUtils.REDCAP_URL), project!!, IntegrationUtils.REDCAP_RECORD_ID_1,
            IntegrationUtils.WORK_PACKAGE + IntegrationUtils.REDCAP_RECORD_ID_1, emptyAttributes
        )
    }

    @Test
    @Throws(IOException::class, URISyntaxException::class)
    fun updateSubjectTest() {
        if (project == null) {
            projectTest()
        }
        val subject =
            mpClient.getSubject(
                URL(IntegrationUtils.REDCAP_URL), IntegrationUtils.REDCAP_PROJECT_ID,
                IntegrationUtils.REDCAP_RECORD_ID_1
            )
        subject?.addAttributes(attributes)
        val newSubject = mpClient.updateSubject(subject!!)

        Assert.assertNotNull(newSubject)
        Assert.assertEquals(subject, newSubject)
        Assert.assertEquals(subject.attributes, newSubject.attributes)
        Assert.assertEquals(subject.sources, newSubject.sources)
    }
}