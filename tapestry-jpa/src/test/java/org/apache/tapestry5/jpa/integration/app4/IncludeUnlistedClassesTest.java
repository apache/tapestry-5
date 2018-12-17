// Copyright 2011 The Apache Software Foundation
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

package org.apache.tapestry5.jpa.integration.app4;

import org.apache.tapestry5.test.SeleniumTestCase;
import org.apache.tapestry5.test.TapestryTestConfiguration;
import org.testng.annotations.Test;

@TapestryTestConfiguration(webAppFolder = "src/test/app4")
public class IncludeUnlistedClassesTest extends SeleniumTestCase
{
    @Test
    public void persist_all() throws Exception
    {
        open("/persistall");

        assertEquals(getText("//span[@id='userName']").length(), 0);
        assertEquals(getText("//span[@id='itemName']").length(), 0);
        assertEquals(getText("//span[@id='thangName']").length(), 0);

        clickAndWait("link=create user");
        assertText("//span[@id='userName']", "Foo User");

        clickAndWait("link=create item");
        assertText("//span[@id='itemName']", "Bar Item");

        clickAndWait("link=create thang");
        assertText("//span[@id='thangName']", "Baz Thang");
    }
}
