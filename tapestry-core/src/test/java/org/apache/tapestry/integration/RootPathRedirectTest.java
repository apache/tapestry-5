// Copyright 2007, 2008 The Apache Software Foundation
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.apache.tapestry.integration;

import org.apache.tapestry.test.AbstractIntegrationTestSuite;
import org.testng.annotations.Test;

@Test(timeOut = 50000, sequential = true, groups = { "integration" })
public class RootPathRedirectTest extends AbstractIntegrationTestSuite
{

    public RootPathRedirectTest()
    {
        super("src/test/app3");
    }


    /**
     * Test to prove that a redirect from the start page works correctly.
     *
     * @see https://issues.apache.org/jira/browse/TAPESTRY-1627
     */
    @Test
    public void redirect_for_root() throws Exception
    {
        open(BASE_URL);

        assertText("//h1", "Login Page");
    }

    @Test
    public void bean_block_overrides()
    {
        start("BeanDisplay Override Demo");

        assertText("//div[@class='t-beandisplay-value no']", "Nay");
        assertText("//div[@class='t-beandisplay-value yes']", "Yea");
    }
}
