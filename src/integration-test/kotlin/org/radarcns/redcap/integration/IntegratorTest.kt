package org.radarcns.redcap.integration

import org.junit.Test
import org.radarcns.redcap.util.IntegrationUtils
import org.radarcns.redcap.util.RedCapTrigger

class IntegratorTest {
    /** This should throw exception as subject already exists in management portal
     * as the [org.radarcns.redcap.EntryPointTest] has run already. The order is
     * specified by [org.radarcns.redcap.testsuite.OrderedTestSuite].
     */
    @Test(expected = IllegalStateException::class)
    fun inputTest() {
        val trigger = RedCapTrigger(IntegrationUtils.TRIGGER_BODY)
        val integrator = Integrator(trigger, IntegrationUtils.mpClient)
        /** This should throw exception as subject already exists in management portal
         * as the [org.radarcns.redcap.EntryPointTest] has run already. The order is
         * specified by [org.radarcns.redcap.testsuite.OrderedTestSuite].
         */
        integrator.handleDataEntryTrigger()
    }
}