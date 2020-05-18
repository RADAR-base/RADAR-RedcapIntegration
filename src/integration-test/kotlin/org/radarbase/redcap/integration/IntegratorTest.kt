package org.radarbase.redcap.integration

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Assert
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.FixMethodOrder
import org.junit.Test
import org.junit.runners.MethodSorters
import org.radarbase.redcap.config.RedCapInfo
import org.radarbase.redcap.config.RedCapManager.getInfo
import org.radarbase.redcap.managementportal.Subject
import org.radarbase.redcap.util.IntegrationUtils
import org.radarbase.redcap.util.IntegrationUtils.LOGGER
import org.radarbase.redcap.util.IntegrationUtils.MP_PROJECT_ID
import org.radarbase.redcap.util.IntegrationUtils.MP_PROJECT_LOCATION
import org.radarbase.redcap.util.IntegrationUtils.REDCAP_RECORD_ID_1
import org.radarbase.redcap.util.IntegrationUtils.REDCAP_RECORD_ID_2
import org.radarbase.redcap.util.IntegrationUtils.TRIGGER_BODY
import org.radarbase.redcap.util.IntegrationUtils.WORK_PACKAGE
import org.radarbase.redcap.util.IntegrationUtils.mpClient
import org.radarbase.redcap.util.IntegrationUtils.updateProjectAttributes
import org.radarbase.redcap.util.RedCapClient
import org.radarbase.redcap.util.RedCapTrigger
import java.io.IOException
import java.net.URISyntaxException
import java.net.URL
import java.util.*

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class IntegratorTest {
    private val mpIntegrator =
        MpIntegrator(mpClient)
    private val trigger = RedCapTrigger(TRIGGER_BODY)
    private val redCapInfo: RedCapInfo = getInfo(trigger)

    private val testAttributes: MutableMap<String, String> = mutableMapOf(
        Pair(
            REDCAP_ATTRIBUTE_1,
            REDCAP_ATTRIBUTE_1_VAL
        ),
        Pair(
            REDCAP_ATTRIBUTE_2,
            REDCAP_ATTRIBUTE_2_VAL
        )
    )

    private val attributeKeys: List<String> = listOf(
        REDCAP_ATTRIBUTE_1,
        REDCAP_ATTRIBUTE_2,
        IntegrationData.SUBJECT_ID_LABEL
    )

    private val redCapClient: RedCapClient = mock {
        on { fetchFormDataForId(attributeKeys, REDCAP_RECORD_ID_1) } doReturn testAttributes
    }
    private val redCapIntegrator: RedCapIntegator =
        RedCapIntegator(redCapClient)

    @Before
    fun init() = updateProjectAttributes()

    @Test
    fun inputTest() {

        // We can check if corresponding subject exists in MP too.
        val subject: Subject? = mpClient.getSubject(
            URL(IntegrationUtils.REDCAP_URL),
            IntegrationUtils.REDCAP_PROJECT_ID,
            REDCAP_RECORD_ID_2
        )

        LOGGER.info("Subject: {}", subject)

        val attr =
            if (subject != null) {
                testAttributes.toMutableMap().apply {
                    this[IntegrationData.SUBJECT_ID_LABEL] = subject.subjectId
                }
            } else testAttributes

        LOGGER.info("Attributes: {}", attr.entries.toTypedArray())

        whenever(
            redCapClient.fetchFormDataForId(
                attributeKeys,
                REDCAP_RECORD_ID_2
            )
        ).thenReturn(
            attr
        )

        val integrator = Integrator(
            trigger = trigger,
            mpClient = mpClient,
            redCapInfo = redCapInfo,
            redCapIntegrator = redCapIntegrator,
            mpIntegrator = mpIntegrator
        )

        // The result will be false since there is no actual redcap instance and hence the form
        // cannot be updated. But even then, no exception should be thrown.
        val result = integrator.handleDataEntryTrigger()
        assertFalse(result)

        Assert.assertNotNull(subject)
        assertEquals(
            Integer.valueOf(REDCAP_RECORD_ID_2),
            subject?.externalId
        )
        assertEquals(
            "$WORK_PACKAGE-$MP_PROJECT_ID-$MP_PROJECT_LOCATION-" +
                    "$REDCAP_RECORD_ID_2",
            subject?.humanReadableIdentifier
        )
        assertEquals("radar", subject?.project?.projectName)
    }

    @Test
    @Throws(IOException::class, URISyntaxException::class)
    fun updateAttributesInMpTest() {
        val attributes: MutableMap<String, String> =
            redCapIntegrator.pullFieldsFromRedcap(attributeKeys, REDCAP_RECORD_ID_1)
        assertEquals(attributes, testAttributes)
        val existingSubjectId: String =
            mpClient.getSubject(redCapInfo.url, redCapInfo.projectId, REDCAP_RECORD_ID_1)!!
                .subjectId
        val subject = mpIntegrator.performSubjectUpdateOnMp(
            redCapInfo.url,
            redCapInfo.projectId,
            REDCAP_RECORD_ID_1,
            attributes,
            existingSubjectId
        )
        assertEquals(subject.attributes, attributes)
        assertEquals(subject.humanReadableIdentifier,
            HUMAN_READABLE_ID
        )
    }

    @Test
    @Throws(IOException::class, URISyntaxException::class)
    fun updateAttributesInMpWhenSubjectExistsTest() {
        testAttributes[REDCAP_ATTRIBUTE_1] =
            REDCAP_ATTRIBUTE_1_VAL_2
        whenever(redCapClient.fetchFormDataForId(attributeKeys, REDCAP_RECORD_ID_1)).thenReturn(
            testAttributes
        )
        val attributes: MutableMap<String, String> =
            redCapIntegrator.pullFieldsFromRedcap(attributeKeys, REDCAP_RECORD_ID_1)

        val existingSubjectId: String =
            mpClient.getSubject(redCapInfo.url, redCapInfo.projectId, REDCAP_RECORD_ID_1)!!
                .subjectId
        val subject = mpIntegrator.performSubjectUpdateOnMp(
            redCapInfo.url,
            redCapInfo.projectId,
            REDCAP_RECORD_ID_1,
            attributes,
            existingSubjectId
        )
        assertEquals(subject.attributes, attributes)
        assertEquals(subject.attributes[REDCAP_ATTRIBUTE_1],
            REDCAP_ATTRIBUTE_1_VAL_2
        )
        assertEquals(subject.humanReadableIdentifier,
            HUMAN_READABLE_ID
        )
    }

    @Test
    @Throws(IOException::class, URISyntaxException::class)
    fun createSubjectWhenSubjectExistsInMpTest() {
        val subject = mpIntegrator.performSubjectUpdateOnMp(
            redCapInfo.url,
            redCapInfo.projectId,
            REDCAP_RECORD_ID_1,
            HashMap(),
            ""
        )
        // This should fail since MP subject exists but REDCap subject does not exists/was deleted
        assertEquals(Subject.SubjectOperationStatus.FAILED, subject.operationStatus)
    }

    companion object {
        private const val REDCAP_ATTRIBUTE_1 = "treatment_arm"
        private const val REDCAP_ATTRIBUTE_1_VAL = "arm1"
        private const val REDCAP_ATTRIBUTE_1_VAL_2 = "arm2"
        private const val REDCAP_ATTRIBUTE_2 = "another_attribute"
        private const val REDCAP_ATTRIBUTE_2_VAL = "test"
        private const val HUMAN_READABLE_ID_KEY = "Human-readable-identifier"
        private const val HUMAN_READABLE_ID: String =
            "$WORK_PACKAGE-$MP_PROJECT_ID-$MP_PROJECT_LOCATION-$REDCAP_RECORD_ID_1"
    }

}