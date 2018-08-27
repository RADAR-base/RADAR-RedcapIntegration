package org.radarcns.redcap.testsuite;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.radarcns.redcap.EntryPointTest;
import org.radarcns.redcap.managementportal.MpClientTest;
import org.radarcns.redcap.integration.IntegratorTest;


/**
 * Test suite to ensure tests are run in a specific order so that various use-cases can be achieved.
 */
@Suite.SuiteClasses({MpClientTest.class, EntryPointTest.class, IntegratorTest.class})
@RunWith(Suite.class)
public class OrderedTestSuite {
}
