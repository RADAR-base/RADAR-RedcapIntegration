package org.radarcns.redcap.testsuite

import org.junit.runner.RunWith
import org.junit.runners.Suite
import org.junit.runners.Suite.SuiteClasses
import org.radarcns.redcap.EntryPointTest
import org.radarcns.redcap.integration.IntegratorTest
import org.radarcns.redcap.managementportal.MpClientTest

/**
 * Test suite to ensure tests are run in a specific order so that various use-cases can be achieved.
 */
@SuiteClasses(MpClientTest::class, EntryPointTest::class, IntegratorTest::class)
@RunWith(Suite::class)
class OrderedTestSuite