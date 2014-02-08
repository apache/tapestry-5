// Copyright 2013 The Apache Software Foundation
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

package org.apache.tapestry5.integration.activationctx2;

import org.apache.tapestry5.integration.TapestryCoreTestCase;
import org.testng.annotations.Test;

/**
 * To test TAP5-2070
 */
public class ActivationContextIntegrationTests2 extends TapestryCoreTestCase
{
    @Test
    public void checked_context_correct()
    {
        openLinks("Context checked correct");

        assertTextPresent("You are able to see me only without activation context");
    }

    @Test
    public void checked_context_error()
    {
        openLinks("Context checked error");

        assertTextPresent("HTTP ERROR 404");
    }

    @Test
    public void unchecked_context_empty()
    {
        openLinks("Context unchecked without");

        assertTextPresent("You are able to see with all context you like...");
    }

    @Test
    public void unchecked_context_one()
    {
        openLinks("Context unchecked with one");

        assertTextPresent("You are able to see with all context you like...");
    }

    @Test
    public void unchecked_context_two()
    {
        openLinks("Context unchecked with two");

        assertTextPresent("You are able to see with all context you like...");
    }
}
