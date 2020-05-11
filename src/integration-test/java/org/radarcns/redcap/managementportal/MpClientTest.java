package org.radarcns.redcap.managementportal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.radarcns.redcap.util.IntegrationUtils.REDCAP_PROJECT_ID;
import static org.radarcns.redcap.util.IntegrationUtils.REDCAP_RECORD_ID_1;
import static org.radarcns.redcap.util.IntegrationUtils.REDCAP_URL;
import static org.radarcns.redcap.util.IntegrationUtils.WORK_PACKAGE;
import static org.radarcns.redcap.util.IntegrationUtils.mpClient;
import static org.radarcns.redcap.util.IntegrationUtils.updateProjectAttributes;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.radarcns.exception.TokenException;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class MpClientTest {
    private Project project;
    private Map<String, String> testAttributes = new HashMap<>();

    @Before
    public void init() throws IOException, TokenException {
        updateProjectAttributes();
    }

    @Test
    public void getProjectTest() throws IOException {
        project = mpClient.getProject(new URL(REDCAP_URL), REDCAP_PROJECT_ID);
        assertEquals("radar", project.getProjectName());
    }

    @Test
    public void createSubjectTest1() throws IOException, URISyntaxException {
        if (project==null) {
            getProjectTest();
        }
        mpClient.createSubject(new URL(REDCAP_URL), project, REDCAP_RECORD_ID_1,
                WORK_PACKAGE + REDCAP_RECORD_ID_1, testAttributes);
        Subject subject = mpClient.getSubject(new URL(REDCAP_URL), REDCAP_PROJECT_ID,
                REDCAP_RECORD_ID_1);

        assertNotNull(subject);
        assertEquals(Integer.valueOf(REDCAP_RECORD_ID_1), subject.getExternalId());
        assertEquals(WORK_PACKAGE + REDCAP_RECORD_ID_1, subject.getHumanReadableIdentifier());
    }

    @Test(expected = IllegalStateException.class)
    public void createSubjectTest2() throws IOException {
        if (project==null) {
            getProjectTest();
        }
        // This should throw exception since subject already exists
        mpClient.createSubject(new URL(REDCAP_URL), project, REDCAP_RECORD_ID_1,
                WORK_PACKAGE + REDCAP_RECORD_ID_1, testAttributes);
    }
}
