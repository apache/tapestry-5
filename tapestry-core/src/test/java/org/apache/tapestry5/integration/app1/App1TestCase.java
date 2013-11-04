package org.apache.tapestry5.integration.app1;

import org.apache.tapestry5.integration.TapestryCoreTestCase;
import org.apache.tapestry5.test.TapestryTestConfiguration;

/**
 * Base class for tests that use the src/test/app1 application; this makes it possible
 * to run the test class directly, as it will have the correct configuration.
 */
@TapestryTestConfiguration(webAppFolder = "src/test/app1")
public abstract class App1TestCase extends TapestryCoreTestCase
{
}
