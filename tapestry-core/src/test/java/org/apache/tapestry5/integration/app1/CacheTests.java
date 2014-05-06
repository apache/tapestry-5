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

public class CacheTests extends App1TestCase
{
    /**
     * TAPESTRY-2338
     */
    @Test
    public void cached_properties_cleared_at_end_of_request()
    {
        openLinks("Clean Cache Demo");

        String time1_1 = getText("time1");
        String time1_2 = getText("time1");

        // Don't know what they are but they should be the same.

        assertEquals(time1_2, time1_1);

        click("link=update");

        waitForAjaxRequestsToComplete();

        String time2_1 = getText("time1");
        String time2_2 = getText("time1");

        // Check that @Cache is still working

        assertEquals(time2_2, time2_1);

        assertFalse(time2_1.equals(time1_1),
                "After update the nanoseconds time did not change, meaning @Cache was broken.");
    }

    @Test
    public void void_method_is_error_with_cached()
    {
        openLinks("@Cached on void method");

        assertTextPresent("Method org.apache.tapestry5.integration.app1.pages.VoidMethodWithCached.invalidMethod()",
                "may not be used with @Cached because it returns void.");
    }

    @Test
    public void parameters_not_allowed_with_cached_method()
    {
        openLinks("@Cached on method with parameters");

        assertTextPresent(
                "Method org.apache.tapestry5.integration.app1.pages.ParamsMethodWithCached.invalidMethod(java.lang.String)",
                "may not be used with @Cached because it has parameters.");
    }
}
