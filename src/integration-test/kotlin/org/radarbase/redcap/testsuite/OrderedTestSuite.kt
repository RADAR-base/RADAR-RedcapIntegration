package org.radarbase.redcap.testsuite

import org.junit.runner.RunWith
import org.junit.runners.Suite
import org.junit.runners.Suite.SuiteClasses
import org.radarbase.redcap.EntryPointTest
import org.radarbase.redcap.integration.IntegratorTest
import org.radarbase.redcap.managementportal.MpClientTest

/**
 * Test suite to ensure tests are run in a specific order so that various use-cases can be achieved.
 */
@SuiteClasses(MpClientTest::class, EntryPointTest::class, IntegratorTest::class)
@RunWith(Suite::class)
class OrderedTestSuite