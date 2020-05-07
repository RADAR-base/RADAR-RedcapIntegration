package org.radarcns.redcap.integration;

import org.junit.Test;
import org.radarcns.redcap.util.RedCapTrigger;

import static org.junit.Assert.assertEquals;
import static org.radarcns.redcap.util.IntegrationUtils.*;

public class IntegratorTest {

    @Test
    public void getInputTest() {
        RedCapTrigger trigger = new RedCapTrigger(TRIGGER_BODY);
        Integrator integrator = new Integrator(trigger, mpClient);

        assertEquals(integrator.handleDataEntryTrigger(), true);
    }
}
