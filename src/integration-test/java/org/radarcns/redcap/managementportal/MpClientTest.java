package org.radarcns.redcap.managementportal;

import org.junit.Before;
import org.junit.Test;
import org.radarcns.exception.TokenException;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

import static org.junit.Assert.*;
import static org.radarcns.redcap.util.IntegrationUtils.*;

public class MpClientTest {
    private static Project project;

    @Before
    public void init() throws IOException, TokenException {
        updateProjectAttributes();
    }

    @Test
    public void getProjectTest() throws MalformedURLException {
        project = mpClient.getProject(new URL(REDCAP_URL), REDCAP_PROJECT_ID, context);
        assertEquals("radar", project.getProjectName());
    }

    @Test
    public void createSubjectTest() throws MalformedURLException, URISyntaxException {
        if(project == null){
            getProjectTest();
        }
        mpClient.createSubject(new URL(REDCAP_URL), project, REDCAP_RECORD_ID_1, WORK_PACKAGE + REDCAP_RECORD_ID_1, context);
        Subject subject = mpClient.getSubject(new URL(REDCAP_URL), REDCAP_PROJECT_ID, REDCAP_RECORD_ID_1, context);

        assertEquals(Integer.valueOf(REDCAP_RECORD_ID_1), subject.getExternalId());
        assertEquals(WORK_PACKAGE + REDCAP_RECORD_ID_1, subject.getHumanReadableIdentifier());
    }
}
