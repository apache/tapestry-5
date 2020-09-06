// Copyright 2020 The Apache Software Foundation
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
package org.apache.tapestry5.http.test;

import org.apache.tapestry5.test.SeleniumTestCase;
import org.apache.tapestry5.test.TapestryTestConfiguration;
import org.testng.annotations.Test;

@Test(singleThreaded = true, groups = "integration")
@TapestryTestConfiguration(webAppFolder = "src/test/webapp")
public class TapestryHttpIntegrationTests extends SeleniumTestCase {

    @Test
    public void testSimpleDispatcher() throws Exception 
    {
        open("/hello");
        assertEquals("Return to base URL", getText("//a"));
        clickAndWait("//a");
        assertEquals("Hello!", getText("//body/a"));
    }

}
