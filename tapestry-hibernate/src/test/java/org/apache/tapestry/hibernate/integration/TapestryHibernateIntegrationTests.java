// Copyright 2007 The Apache Software Foundation
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

package org.apache.tapestry.hibernate.integration;

import org.apache.tapestry.test.AbstractIntegrationTestSuite;
import org.testng.annotations.Test;

@Test
public class TapestryHibernateIntegrationTests extends AbstractIntegrationTestSuite
{
    public TapestryHibernateIntegrationTests()
    {
        super("src/test/webapp");
    }

	public void test_valueencode_all_entity_types() throws Exception {
		open("/encodeentities");
		
		assertEquals(0, getText("//span[@id='name']").length());

		// need to create an entity in order to link with one
		clickAndWait("//a[@id='createentity']");
		assertEquals("name", getText("//span[@id='name']"));
		
		// should return null for missing objects
		open("/encodeentities/9999");
		assertEquals(0, getText("//span[@id='name']").length());
	}

}
