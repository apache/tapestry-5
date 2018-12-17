// Copyright 2011 The Apache Software Foundation
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

package org.apache.tapestry5.jpa.integration.app2;

import org.apache.tapestry5.test.SeleniumTestCase;
import org.apache.tapestry5.test.TapestryTestConfiguration;
import org.testng.annotations.Test;

@TapestryTestConfiguration(webAppFolder = "src/test/app2")
public class SinglePersistenceUnitIntegrationTest extends SeleniumTestCase
{

    @Test
    public void persist_entities()
    {
        open("/persistitem");
        assertEquals(getText("//span[@id='name']").length(), 0);

        clickAndWait("link=create item");
        assertText("//span[@id='name']", "name");
    }

    @Test
    public void inject_into_page_wihout_jpa_annotationt()
    {
        open("/persistitem2");
        assertEquals(getText("//span[@id='name']").length(), 0);

        clickAndWait("link=create item");
        assertText("//span[@id='name']", "name");
    }

    @Test
    public void inject_into_service_wihout_jpa_annotationt()
    {
        open("/persistitem3");
        assertEquals(getText("//span[@id='name']").length(), 0);

        clickAndWait("link=create item");
        assertText("//span[@id='name']", "name");
    }
}
