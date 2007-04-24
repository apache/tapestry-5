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

import java.lang.reflect.Method;

import org.apache.commons.logging.Log;
import org.apache.tapestry.ioc.OrderedConfiguration;
import org.apache.tapestry.ioc.def.ContributionDef;
import org.testng.annotations.Test;

public class ValidatingOrderedConfigurationWrapperTest extends IOCInternalTestCase
{
    @Test
    public void valid_type_long_form()
    {
        ContributionDef def = newContributionDef();
        Log log = newLog();
        OrderedConfiguration<Runnable> configuration = newOrderedConfiguration();
        Runnable contribution = newRunnable();

        configuration.add("id", contribution, "after:pre", "before:post");

        replay();

        OrderedConfiguration<Runnable> wrapper = new ValidatingOrderedConfigurationWrapper<Runnable>(
                "Service", def, log, Runnable.class, configuration);

        wrapper.add("id", contribution, "after:pre", "before:post");

        verify();
    }

    @Test
    public void valid_type_short_form()
    {
        ContributionDef def = newContributionDef();
        Log log = newLog();
        OrderedConfiguration<Runnable> configuration = newOrderedConfiguration();
        Runnable contribution = newRunnable();

        configuration.add("id", contribution);

        replay();

        OrderedConfiguration<Runnable> wrapper = new ValidatingOrderedConfigurationWrapper<Runnable>(
                "Service", def, log, Runnable.class, configuration);

        wrapper.add("id", contribution);

        verify();
    }

    @Test
    public void null_object_passed_through()
    {
        ContributionDef def = newContributionDef();
        Log log = newLog();
        OrderedConfiguration<Runnable> configuration = newOrderedConfiguration();

        configuration.add("id", null);

        replay();

        OrderedConfiguration<Runnable> wrapper = new ValidatingOrderedConfigurationWrapper<Runnable>(
                "Service", def, log, Runnable.class, configuration);

        wrapper.add("id", null);

        verify();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void incorrect_contribution_type_is_passed_through_as_null()
    {
        Method method = findMethod("contributeBarneyService");

        ContributionDef def = new ContributionDefImpl("Service", method, getClassFactory());
        Log log = newLog();
        OrderedConfiguration<Runnable> configuration = newOrderedConfiguration();

        log.warn(IOCMessages.contributionWrongValueType(
                "Service",
                def,
                String.class,
                Runnable.class));

        configuration.add("id", null);

        replay();

        OrderedConfiguration wrapper = new ValidatingOrderedConfigurationWrapper("Service", def,
                log, Runnable.class, configuration);

        wrapper.add("id", "string");

        verify();
    }

    public void contributeBarneyService(OrderedConfiguration<Runnable> configuration)
    {

    }
}
