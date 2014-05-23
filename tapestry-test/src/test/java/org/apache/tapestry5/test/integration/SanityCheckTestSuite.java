// Copyright 2009, 2012 The Apache Software Foundation
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.apache.tapestry5.test.integration;

import org.apache.tapestry5.test.SeleniumTestCase;
import org.apache.tapestry5.test.TapestryTestConfiguration;
import org.apache.tapestry5.test.TapestryTestConstants;
import org.testng.ITestContext;
import org.testng.annotations.Test;

@TapestryTestConfiguration(webAppFolder = "src/test/webapp")
public class SanityCheckTestSuite extends SeleniumTestCase
{
    @Test
    public void sanity_check()
    {
        openBaseURL();

        assertText("//h1", "Tapestry Test");
    }

    @Test
    public void invalid_assertion()
    {
        openBaseURL();

        try
        {
            assertText("//h1", "XYZ");
            unreachable();
        }
        catch (AssertionError ex)
        {
            assertEquals(ex.getMessage(), "//h1 was 'Tapestry Test' not 'XYZ'");
        }
    }

    @Test
    public void command_processor_available(ITestContext context)
    {
        assertNotNull(context.getAttribute(TapestryTestConstants.COMMAND_PROCESSOR_ATTRIBUTE));
    }

    @Override
    public Number getCssCount(String str) {
        return selenium.getCssCount(str);
    }
}
