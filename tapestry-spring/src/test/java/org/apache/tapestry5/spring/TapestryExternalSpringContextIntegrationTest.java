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

package org.apache.tapestry5.spring;

import org.apache.tapestry5.test.AbstractIntegrationTestSuite;
import org.testng.annotations.Test;

public class TapestryExternalSpringContextIntegrationTest extends AbstractIntegrationTestSuite
{
    public TapestryExternalSpringContextIntegrationTest()
    {
        super("src/test/webapp1");
    }

    @Test
    public void external_context_integration_test() throws Exception
    {
        open(BASE_URL);

        assertTextPresent("Demonstrates Spring Context Configured Externally", "Instantiated via a factory bean.");
    }
}
