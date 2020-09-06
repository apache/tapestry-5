// Copyright 2008-2014 The Apache Software Foundation
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

package org.apache.tapestry5.hibernate.web.integration;

import org.apache.tapestry5.hibernate.web.internal.PersistedEntity;
import org.apache.tapestry5.hibernate.web.internal.PersistedTransientEntity;
import org.apache.tapestry5.test.SeleniumTestCase;
import org.apache.tapestry5.test.TapestryTestConfiguration;
import org.example.app0.entities.User;
import org.testng.annotations.Test;

@Test(sequential = true, groups = "integration")
@TapestryTestConfiguration(webAppFolder = "src/test/webapp")
public class TapestryHibernateIntegrationTests extends SeleniumTestCase
{

    private final String PERSISTENT_ENTITY_CLASS_NAME = PersistedEntity.class.getName();
    private final String PERSISTED_TRANSIENT_ENTITY_CLASS_NAME = PersistedTransientEntity.class.getName();

    public void valueencode_all_entity_types() throws Exception
    {
        open("/encodeentities");

        assertEquals(getText("//span[@id='name']").length(), 0);

        // need to create an entity in order to link with one
        clickAndWait("link=create an entity");
        assertEquals(getText("//span[@id='name']"), "name");

        // should return null for missing objects
        open("/encodeentities/9999");
        assertEquals(getText("//span[@id='name']").length(), 0);
    }

    public void persist_entities()
    {
        open("/persistentity");
        assertEquals(getText("//span[@id='name']").length(), 0);

        clickAndWait("link=create entity");
        assertText("//span[@id='name']", "name");

        // shouldn't save the change to the name because it's reloaded every time
        clickAndWait("link=change the name");
        assertText("//span[@id='name']", "name");

        // can set back to null
        clickAndWait("link=set to null");
        assertEquals(getText("//span[@id='name']").length(), 0);

        // deleting an entity that is still persisted. just remove the entity from the session if it's not found.
        clickAndWait("link=create entity");
        assertText("//span[@id='name']", "name");
        clickAndWait("link=delete");
        assertEquals(getText("//span[@id='name']").length(), 0);
    }

    public void sso_entities()
    {
        open("/ssoentity");
        assertEquals(getText("//span[@id='name']").length(), 0);
        assertText("//span[@id='persistedEntityClassName']", PERSISTED_TRANSIENT_ENTITY_CLASS_NAME);

        clickAndWait("link=persist entity");
        assertText("//span[@id='name']", "name");
        assertText("//span[@id='persistedEntityClassName']", PERSISTENT_ENTITY_CLASS_NAME);

        // can set back to null
        clickAndWait("link=set to null");
        assertEquals(getText("//span[@id='name']").length(), 0);
        assertText("//span[@id='persistedEntityClassName']", PERSISTED_TRANSIENT_ENTITY_CLASS_NAME);

        clickAndWait("link=persist entity");
        assertText("//span[@id='name']", "name");
        assertText("//span[@id='persistedEntityClassName']", PERSISTENT_ENTITY_CLASS_NAME);
        clickAndWait("link=delete");
        assertEquals(getText("//span[@id='name']").length(), 0);
        assertText("//span[@id='persistedEntityClassName']", PERSISTED_TRANSIENT_ENTITY_CLASS_NAME);

        clickAndWait("link=persist entity");
        assertText("//span[@id='name']", "name");
        assertText("//span[@id='persistedEntityClassName']", PERSISTENT_ENTITY_CLASS_NAME);
    }

    /**
     * TAPESTRY-2244
     */
    public void using_cached_with_form()
    {
        openLinks("Cached Form", "setup");
        assertTextSeries("name_%d", 0);

        type("name", "name1");
        clickAndWait(SUBMIT);
        assertTextSeries("name_%d", 0, "name1");

        type("name", "name2");
        clickAndWait(SUBMIT);
        assertTextSeries("name_%d", 0, "name1", "name2");
    }

    public void commit_after_on_component_methods()
    {
        openLinks("CommitAfter Demo");

        assertText("name", "Diane");

        clickAndWait("link=change name");

        assertText("name", "Frank");

        clickAndWait("link=runtime exception");

        assertText("name", "Frank");

        clickAndWait("link=checked exception");

        assertText("name", "Troy");

    }

    public void grid()
    {
        openLinks("Grid Demo", "setup");

        clickAndWait("link=First Name");

        assertText("//td[@data-grid-column-sort='ascending']", "Joe_1");

        clickAndWait("link=First Name");

        assertText("//td[@data-grid-column-sort='descending']", "Joe_9");
    }

    public void hibernate_statistics()
    {
        open(getBaseURL() + "t5dashboard/hibernate");

        assertTextPresent("Entities Statistics");

        assertTextPresent(User.class.getName());
    }

    protected final void assertTextSeries(String idFormat, int startIndex, String... values)
    {
        for (int i = 0; i < values.length; i++)
        {
            String id = String.format(idFormat, startIndex + i);

            assertText(id, values[i]);
        }
    }


}
