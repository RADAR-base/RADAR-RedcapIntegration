package org.radarcns.redcap.managementportal;

import okhttp3.*;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.radarcns.exception.TokenException;
import org.radarcns.redcap.config.Properties;
import org.radarcns.redcap.listener.TokenManagerListener;

import javax.servlet.ServletContext;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

import static org.junit.Assert.*;
import static org.radarcns.redcap.util.IntegrationUtils.*;

public class MpClientTest {

    private static ServletContext context = Mockito.mock(ServletContext.class);


    private static  final OkHttpClient httpClient = new OkHttpClient();
    private static Project project;
    private static MpClient mpClient;

    @Before
    public void init() throws IOException, TokenException {

        context.setAttribute(HTTP_CLIENT, httpClient);

        mpClient = new MpClient(httpClient);
        updateProjectAttributes(httpClient, context);
    }

    @Test
    public void getProjectTest() throws MalformedURLException {
        project = mpClient.getProject(new URL(REDCAP_URL), REDCAP_PROJECT_ID, context);
        assertEquals(project.getProjectName(), "radar");
    }

    @Test
    public void createSubjectTest() throws MalformedURLException, URISyntaxException {
        if(project == null){
            getProjectTest();
        }
        mpClient.createSubject(new URL(REDCAP_URL), project, REDCAP_RECORD_ID, WORK_PACKAGE + REDCAP_RECORD_ID, context);
        Subject subject = mpClient.getSubject(new URL(REDCAP_URL), REDCAP_PROJECT_ID, REDCAP_RECORD_ID, context);

        assertEquals(subject.getExternalId(), Integer.valueOf(REDCAP_RECORD_ID));
        assertEquals(subject.getHumanReadableIdentifier(), WORK_PACKAGE + REDCAP_RECORD_ID);
    }
}
