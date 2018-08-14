package org.radarcns.redcap;

import okhttp3.Response;
import org.junit.Test;
import org.radarcns.redcap.managementportal.Subject;
import org.radarcns.redcap.util.IntegrationClient;
import javax.ws.rs.core.MediaType;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

import static org.junit.Assert.assertEquals;
import static org.radarcns.redcap.util.IntegrationUtils.*;

public class EntryPointTest {

    @Test
    public void triggerTest() throws MalformedURLException, URISyntaxException {
        Response response = IntegrationClient.makeTriggerRequest(TRIGGER_BODY, MediaType.TEXT_PLAIN);

        // This is because the the app fails to update redcap form as there is no actual redcap instance.
        assertEquals(500, response.code());

        // But we can verify if the corresponding subject creation in MP works fine.
        Subject subject = mpClient.getSubject(new URL(REDCAP_URL), REDCAP_PROJECT_ID, REDCAP_RECORD_ID_2, context);

        assertEquals(Integer.valueOf(REDCAP_RECORD_ID_2), subject.getExternalId());
        assertEquals(WORK_PACKAGE + "-" + MP_PROJECT_ID + "-" + MP_PROJECT_LOCATION + "-" + REDCAP_RECORD_ID_2
                , subject.getHumanReadableIdentifier());
        assertEquals("radar", subject.getProject().getProjectName());
    }
}
