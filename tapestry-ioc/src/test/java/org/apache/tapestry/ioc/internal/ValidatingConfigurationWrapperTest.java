// Copyright 2006, 2007 The Apache Software Foundation
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

package org.apache.tapestry.ioc.internal;

import org.apache.commons.logging.Log;
import org.apache.tapestry.ioc.Configuration;
import org.apache.tapestry.ioc.def.ContributionDef;
import org.testng.annotations.Test;

public class ValidatingConfigurationWrapperTest extends IOCInternalTestCase
{
    @SuppressWarnings("unchecked")
    @Test
    public void valid_contribution()
    {
        ContributionDef def = newContributionDef();
        Log log = newLog();
        Configuration configuration = newConfiguration();
        Runnable value = newRunnable();

        configuration.add(value);

        replay();

        Configuration wrapper = new ValidatingConfigurationWrapper("foo.Bar", log, Runnable.class,
                def, configuration);

        wrapper.add(value);

        verify();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void null_contribution()
    {
        Log log = newLog();
        Configuration configuration = newConfiguration();
        ContributionDef def = new ContributionDefImpl("Bar", findMethod("contributeUnorderedNull"),
                getClassFactory());

        log.warn(IOCMessages.contributionWasNull("Bar", def));

        replay();

        Configuration wrapper = new ValidatingConfigurationWrapper("Bar", log, Runnable.class, def,
                configuration);

        wrapper.add(null);

        verify();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void wrong_type_of_contribution()
    {
        Log log = newLog();
        Configuration configuration = newConfiguration();
        ContributionDef def = new ContributionDefImpl("Bar", findMethod("contributeUnorderedNull"),
                getClassFactory());

        log.warn(IOCMessages.contributionWrongValueType("Bar", def, String.class, Runnable.class));

        replay();

        Configuration wrapper = new ValidatingConfigurationWrapper("Bar", log, Runnable.class, def,
                configuration);

        wrapper.add("runnable");

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
