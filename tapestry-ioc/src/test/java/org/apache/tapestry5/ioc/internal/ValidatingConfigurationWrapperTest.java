// Copyright 2006, 2007, 2008 The Apache Software Foundation
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

package org.apache.tapestry5.ioc.internal;

import org.apache.tapestry5.ioc.Configuration;
import org.apache.tapestry5.ioc.def.ContributionDef;
import org.slf4j.Logger;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

public class ValidatingConfigurationWrapperTest extends IOCInternalTestCase
{
    @SuppressWarnings("unchecked")
    @Test
    public void valid_contribution()
    {
        Configuration configuration = mockConfiguration();
        Runnable value = mockRunnable();

        configuration.add(value);

        replay();

        Configuration wrapper = new ValidatingConfigurationWrapper("foo.Bar",
                                                                   Runnable.class, configuration);

        wrapper.add(value);

        verify();
    }

    @SuppressWarnings({"unchecked"})
    @Test
    public void valid_class_contribution()
    {
        ContributionDef def = mockContributionDef();
        Logger logger = mockLogger();
        Configuration configuration = mockConfiguration();

        configuration.addInstance(HashMap.class);

        replay();

        Configuration wrapper = new ValidatingConfigurationWrapper("foo.Bar",
                                                                   Map.class, configuration);

        wrapper.addInstance(HashMap.class);

        verify();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void null_contribution()
    {
        Configuration configuration = mockConfiguration();

        replay();

        Configuration wrapper = new ValidatingConfigurationWrapper("Bar", Runnable.class,
                                                                   configuration);

        try
        {
            wrapper.add(null);
            unreachable();
        }
        catch (NullPointerException ex)
        {
            assertEquals(ex.getMessage(), "Service contribution (to service 'Bar') was null.");
        }

        verify();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void wrong_type_of_contribution()
    {
        Configuration configuration = mockConfiguration();

        replay();

        Configuration wrapper = new ValidatingConfigurationWrapper("Bar", Runnable.class,
                                                                   configuration);

        try
        {
            wrapper.add("runnable");
            unreachable();
        }
        catch (IllegalArgumentException ex)
        {
            assertEquals(ex.getMessage(),
                         "Service contribution (to service 'Bar') was an instance of java.lang.String, which is not assignable to the configuration type java.lang.Runnable.");
        }

        verify();
    }

    @Test
    public void wrong_class_contributed()
    {
        Configuration configuration = mockConfiguration();

        replay();

        Configuration wrapper = new ValidatingConfigurationWrapper("Bar", Runnable.class,
                                                                   configuration);

        try
        {
            wrapper.addInstance(this.getClass());
            unreachable();
        }
        catch (IllegalArgumentException ex)
        {
            assertMessageContains(ex,
                                  "Unable to contribute instance of class org.apache.tapestry5.ioc.internal.ValidatingConfigurationWrapperTest",
                                  "to service 'Bar'",
                                  "as it is not assignable to expected contribution type java.lang.Runnable");
        }

        verify();
    }

    // Just a placeholder to give the errors something to report about

    public void contributeUnorderedNull()
    {

    }

    public void contributeWrongType()
    {

    }
}
