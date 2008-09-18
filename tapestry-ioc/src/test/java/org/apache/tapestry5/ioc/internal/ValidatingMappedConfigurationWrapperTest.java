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

import org.apache.tapestry5.ioc.MappedConfiguration;
import org.apache.tapestry5.ioc.ObjectLocator;
import org.apache.tapestry5.ioc.def.ContributionDef;
import static org.apache.tapestry5.ioc.internal.util.CollectionFactory.newMap;
import org.slf4j.Logger;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Map;

public class ValidatingMappedConfigurationWrapperTest extends IOCInternalTestCase
{
    private static final String SERVICE_ID = "Baz";

    @Test
    public void proper_key_and_value()
    {
        ContributionDef def = mockContributionDef();
        Logger logger = mockLogger();
        Map<Class, ContributionDef> keyToContribution = newMap();
        MappedConfiguration<Class, Runnable> delegate = mockMappedConfiguration();
        ObjectLocator locator = mockObjectLocator();

        Class key = Integer.class;
        Runnable value = mockRunnable();

        delegate.add(key, value);

        replay();

        MappedConfiguration<Class, Runnable> wrapper = new ValidatingMappedConfigurationWrapper<Class, Runnable>(
                SERVICE_ID, def, Class.class, Runnable.class, keyToContribution, delegate, locator);

        wrapper.add(key, value);

        verify();

        assertSame(keyToContribution.get(Integer.class), def);
    }

    @Test
    public void duplicate_key()
    {
        ContributionDef def1 = newContributionDef("contributionPlaceholder1");
        ContributionDef def2 = newContributionDef("contributionPlaceholder2");
        Map<Class, ContributionDef> keyToContribution = newMap();
        ObjectLocator locator = mockObjectLocator();

        keyToContribution.put(Integer.class, def1);

        MappedConfiguration<Class, Runnable> delegate = mockMappedConfiguration();

        Class key = Integer.class;
        Runnable value = mockRunnable();

        replay();

        MappedConfiguration<Class, Runnable> wrapper = new ValidatingMappedConfigurationWrapper<Class, Runnable>(
                SERVICE_ID, def2, Class.class, Runnable.class, keyToContribution, delegate, locator);

        try
        {
            wrapper.add(key, value);
            unreachable();
        }
        catch (IllegalArgumentException ex)
        {
            assertMessageContains(ex,
                                  "Service contribution (to service 'Baz') conflicts with existing contribution");
        }

        verify();

        assertSame(keyToContribution.get(Integer.class), def1);
    }

    @Test
    public void null_key()
    {
        ContributionDef def = newContributionDef("contributionPlaceholder1");
        Map<Class, ContributionDef> keyToContribution = newMap();
        MappedConfiguration<Class, Runnable> delegate = mockMappedConfiguration();
        Runnable value = mockRunnable();
        ObjectLocator locator = mockObjectLocator();

        replay();

        MappedConfiguration<Class, Runnable> wrapper = new ValidatingMappedConfigurationWrapper<Class, Runnable>(
                SERVICE_ID, def, Class.class, Runnable.class, keyToContribution, delegate, locator);

        try
        {
            wrapper.add(null, value);
            unreachable();
        }
        catch (NullPointerException ex)
        {
            assertEquals(ex.getMessage(), "Key for service contribution (to service 'Baz') was null.");
        }

        verify();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void wrong_key_type()
    {
        ContributionDef def = newContributionDef("contributionPlaceholder1");
        Map<?, ContributionDef> keyToContribution = newMap();
        MappedConfiguration delegate = mockMappedConfiguration();
        Runnable value = mockRunnable();
        ObjectLocator locator = mockObjectLocator();

        replay();

        MappedConfiguration wrapper = new ValidatingMappedConfigurationWrapper(SERVICE_ID, def,
                                                                               Class.class, Runnable.class,
                                                                               keyToContribution, delegate, locator);

        try
        {
            wrapper.add("java.util.List", value);
            unreachable();
        }
        catch (IllegalArgumentException ex)
        {
            assertEquals(ex.getMessage(),
                         "Key for service contribution (to service 'Baz') was an instance of java.lang.String, but the expected key type was java.lang.Class.");
        }

        verify();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void wrong_value_type()
    {
        ContributionDef def = newContributionDef("contributionPlaceholder1");
        Map<?, ContributionDef> keyToContribution = newMap();
        MappedConfiguration delegate = mockMappedConfiguration();
        ObjectLocator locator = mockObjectLocator();


        replay();

        MappedConfiguration wrapper = new ValidatingMappedConfigurationWrapper(SERVICE_ID, def,
                                                                               Class.class, Runnable.class,
                                                                               keyToContribution, delegate, locator);

        try
        {
            wrapper.add(List.class, "do something");
            unreachable();
        }
        catch (IllegalArgumentException ex)
        {
            assertEquals(ex.getMessage(),
                         "Service contribution (to service 'Baz') was an instance of java.lang.String, which is not assignable to the configuration type java.lang.Runnable.");
        }

        verify();
    }

    @Test
    public void null_value()
    {
        ContributionDef def = newContributionDef("contributionPlaceholder1");
        Map<Class, ContributionDef> keyToContribution = newMap();
        MappedConfiguration<Class, Runnable> delegate = mockMappedConfiguration();
        ObjectLocator locator = mockObjectLocator();

        replay();

        MappedConfiguration<Class, Runnable> wrapper = new ValidatingMappedConfigurationWrapper<Class, Runnable>(
                SERVICE_ID, def, Class.class, Runnable.class, keyToContribution, delegate, locator);

        try
        {
            wrapper.add(Integer.class, null);
            unreachable();
        }
        catch (NullPointerException ex)
        {
            assertEquals(ex.getMessage(), "Service contribution (to service 'Baz') was null.");
        }

        verify();
    }

    private ContributionDef newContributionDef(String methodName)
    {
        return new ContributionDefImpl(SERVICE_ID, findMethod(methodName), getClassFactory());
    }

    public void contributionPlaceholder1()
    {

    }

    public void contributionPlaceholder2()
    {

    }
}
