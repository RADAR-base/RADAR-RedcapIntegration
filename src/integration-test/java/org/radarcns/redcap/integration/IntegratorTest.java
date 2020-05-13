package org.radarcns.redcap.integration;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.radarcns.exception.TokenException;
import org.radarcns.redcap.config.RedCapInfo;
import org.radarcns.redcap.config.RedCapManager;
import org.radarcns.redcap.managementportal.Subject;
import org.radarcns.redcap.util.RedCapClient;
import org.radarcns.redcap.util.RedCapTrigger;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.radarcns.redcap.util.IntegrationUtils.*;

public class IntegratorTest {
    private Map<String, String> testAttributes = new HashMap<>();
    private RedCapClient redCapClient;
    private RedCapIntegator redCapIntegrator;
    private MpIntegrator mpIntegrator = new MpIntegrator(mpClient);
    private List<String> attributeKeys;
    public static RedCapTrigger trigger = new RedCapTrigger(TRIGGER_BODY);
    public static final RedCapInfo redCapInfo = RedCapManager.getInfo(trigger);




    public static final String REDCAP_ATTRIBUTE_1 = "treatment_arm";
    public static final String REDCAP_ATTRIBUTE_1_VAL = "arm1";
    public static final String REDCAP_ATTRIBUTE_1_VAL_2 = "arm2";
    public static final String REDCAP_ATTRIBUTE_2 = "another_attribute";
    public static final String REDCAP_ATTRIBUTE_2_VAL = "test";

    public static final String HUMAN_READABLE_ID_KEY = "Human-readable-identifier";
    public static final String HUMAN_READABLE_ID = WORK_PACKAGE + "-" + MP_PROJECT_ID + "-" + MP_PROJECT_LOCATION + "-"+ REDCAP_RECORD_ID_2;
    public static final String REDCAP_SUBJECT_ID_FIELD = "subject_id";

    @Before
    public void init() throws IOException, TokenException {
        redCapClient = Mockito.mock(RedCapClient.class);
        redCapIntegrator = new RedCapIntegator(redCapClient);
        testAttributes.put(REDCAP_ATTRIBUTE_1,REDCAP_ATTRIBUTE_1_VAL);
        testAttributes.put(REDCAP_ATTRIBUTE_2,REDCAP_ATTRIBUTE_2_VAL);
        testAttributes.put(HUMAN_READABLE_ID_KEY,HUMAN_READABLE_ID);
        attributeKeys = redCapInfo.getAttributeFieldNames();

        updateProjectAttributes();
    }

    @Test
    public void getInputTest() {
        Integrator integrator = new Integrator(trigger, mpClient);

        assertEquals(integrator.handleDataEntryTrigger(), false);
    }

    @Test
    public void updateAttributesInMpTest() throws IOException, URISyntaxException {
        Mockito.when(redCapClient.fetchFormDataForId(attributeKeys, REDCAP_RECORD_ID_2)).thenReturn(testAttributes);
        Map<String, String> attributes = redCapIntegrator.pullRecordAttributes(attributeKeys, REDCAP_RECORD_ID_2);

        assertEquals(testAttributes, attributes);

        Subject subject = mpIntegrator.performSubjectUpdateOnMp(redCapInfo.getUrl(), redCapInfo.getProjectId(), REDCAP_RECORD_ID_2, attributes, "");

        assertEquals(attributes, subject.getAttributes());

        assertEquals(HUMAN_READABLE_ID, subject.getHumanReadableIdentifier());
    }

    @Test
    public void updateAttributesInMpWhenSubjectExistsTest() throws IOException, URISyntaxException {
        testAttributes.put(REDCAP_ATTRIBUTE_1, REDCAP_ATTRIBUTE_1_VAL_2);
        Mockito.when(redCapClient.fetchFormDataForId(attributeKeys, REDCAP_RECORD_ID_2)).thenReturn(testAttributes);
        Map<String, String> attributes = redCapIntegrator.pullRecordAttributes(attributeKeys, REDCAP_RECORD_ID_2);

        String existingSubjectId = mpClient.getSubject(redCapInfo.getUrl(), redCapInfo.getProjectId(), REDCAP_RECORD_ID_2).getSubjectId();

        Subject subject = mpIntegrator.performSubjectUpdateOnMp(redCapInfo.getUrl(), redCapInfo.getProjectId(), REDCAP_RECORD_ID_2, attributes,existingSubjectId);

        assertEquals(attributes, subject.getAttributes());
        assertEquals(REDCAP_ATTRIBUTE_1_VAL_2, subject.getAttributes().get(REDCAP_ATTRIBUTE_1));

        assertEquals(HUMAN_READABLE_ID, subject.getHumanReadableIdentifier());
    }

    @Test
    public void createSubjectWhenSubjectExistsInMpTest() throws IOException, URISyntaxException {
        Subject subject = mpIntegrator.performSubjectUpdateOnMp(redCapInfo.getUrl(), redCapInfo.getProjectId(), REDCAP_RECORD_ID_2, new HashMap<>(),"");

        // This should fail since MP project exists but REDCap project does not exists/was deleted
        assertEquals(Subject.SubjectOperationStatus.FAILED, subject.getOprationStatus());
    }
}
