// Copyright 2023 The Apache Software Foundation
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

package org.apache.tapestry5.jpa.integration.app6;

import org.apache.tapestry5.test.SeleniumTestCase;
import org.apache.tapestry5.test.TapestryTestConfiguration;
import org.example.app6.pages.Sign;
import org.testng.annotations.Test;

@TapestryTestConfiguration(webAppFolder = "src/test/app6")
public class SessionApplicationStatePersistenceStrategyTest extends SeleniumTestCase
{

    @Test
    public void check()
    {
        deleteAllVisibleCookies();
        open("/sign");
        clickAndWait("link=Sign in");
        String firstname = "test-firstname";
        typeKeys("//input[@id='firstname']", firstname);
        typeKeys("//input[@id='lastname']", "test-lastname");
        clickAndWait("//input[@id='register']");
        assertTextPresent(Sign.GREETING + firstname);
    }
}
