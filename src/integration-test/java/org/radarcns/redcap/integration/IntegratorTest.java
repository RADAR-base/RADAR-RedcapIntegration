package org.radarcns.redcap.integration;

import org.junit.Test;
import org.radarcns.redcap.util.RedCapTrigger;

import static org.radarcns.redcap.util.IntegrationUtils.TRIGGER_BODY;
import static org.radarcns.redcap.util.IntegrationUtils.httpClient;
import static org.radarcns.redcap.util.IntegrationUtils.mpClient;

public class IntegratorTest {

    @Test(expected = IllegalStateException.class)
    public void getInputTest() {
        RedCapTrigger trigger = new RedCapTrigger(TRIGGER_BODY);
        Integrator integrator = new Integrator(trigger, mpClient, httpClient);

        /** This should throw exception as subject already exists in management portal
         * as the {@link org.radarcns.redcap.EntryPointTest} has run already. The order is
         * specifies by {@link org.radarcns.redcap.testsuite.OrderedTestSuite}.
         **/
        integrator.getInput();
    }
}
