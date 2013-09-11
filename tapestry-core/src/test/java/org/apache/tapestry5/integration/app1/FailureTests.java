// Copyright 2010-2013 The Apache Software Foundation
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

package org.apache.tapestry5.integration.app1;

import org.testng.annotations.Test;

/**
 * Tests for various kinds of failure conditions. Generally, what's being tested is that the exception
 * is being reported correctly.
 */
public class FailureTests extends App1TestCase
{
    /**
     * With lt;span t:id="foo"/&gt; in the template, there should be an @Component
     * in the Java class. When there's not, it's an error.
     */
    @Test
    public void component_id_in_template_with_no_type()
    {
        openLinks("No Component Type Provided");

        assertTextPresent("org.apache.tapestry5.integration.app1.pages.NoTypeProvidedDemo",
                "You must specify the type via t:type, the element, or @Component annotation.");
    }

}
