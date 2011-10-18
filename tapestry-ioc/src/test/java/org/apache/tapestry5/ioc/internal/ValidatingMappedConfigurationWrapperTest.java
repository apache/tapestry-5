// Copyright 2006, 2007, 2008, 2009, 2011 The Apache Software Foundation
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

package org.apache.tapestry5.ioc.internal;

import org.apache.tapestry5.ioc.MappedConfiguration;
import org.apache.tapestry5.ioc.ObjectLocator;
import org.apache.tapestry5.ioc.def.ContributionDef;
import org.apache.tapestry5.ioc.internal.util.CollectionFactory;
import org.testng.annotations.Test;

import java.util.Map;

import static org.apache.tapestry5.ioc.internal.util.CollectionFactory.newMap;

@SuppressWarnings(
        {"rawtypes", "unchecked"})
public class ValidatingMappedConfigurationWrapperTest extends IOCInternalTestCase
{
    private static final String SERVICE_ID = "Baz";

    @Test
    public void proper_key_and_value()
    {
        ContributionDef def = mockContributionDef();
        Map<Class, ContributionDef> keyToContribution = newMap();
        ObjectLocator locator = mockObjectLocator();
        Map<Class, Runnable> map = CollectionFactory.newMap();
        TypeCoercerProxy tc = mockTypeCoercerProxy();

        Class key = Integer.class;
        Runnable value = mockRunnable();

        expect(tc.coerce(value, Runnable.class)).andReturn(value);

        replay();

        MappedConfiguration<Class, Runnable> wrapper = new ValidatingMappedConfigurationWrapper<Class, Runnable>(
                Runnable.class, locator, tc, map, null, SERVICE_ID, def, Class.class, keyToContribution);

        wrapper.add(key, value);

        verify();

        assertSame(map.get(key), value);
        assertSame(keyToContribution.get(Integer.class), def);
    }

    @Test
    public void coerced_value()
    {
        ContributionDef def = mockContributionDef();
        Map<Class, ContributionDef> keyToContribution = newMap();
        ObjectLocator locator = mockObjectLocator();
        Map<Class, Runnable> map = CollectionFactory.newMap();
        TypeCoercerProxy tc = mockTypeCoercerProxy();
        String contributedValue = "coerceme";

        Class key = Integer.class;
        Runnable value = mockRunnable();

        expect(tc.coerce(contributedValue, Runnable.class)).andReturn(value);

        replay();

        MappedConfiguration<Class, Object> wrapper = new ValidatingMappedConfigurationWrapper(Runnable.class, locator,
                tc, map, null, SERVICE_ID, def, Class.class, keyToContribution);

        wrapper.add(key, contributedValue);

        verify();

        assertSame(map.get(key), value);
        assertSame(keyToContribution.get(Integer.class), def);
    }

    @Test
    public void duplicate_key()
    {
        ContributionDef def1 = newContributionDef("contributionPlaceholder1");
        ContributionDef def2 = newContributionDef("contributionPlaceholder2");
        Map<Class, ContributionDef> keyToContribution = newMap();
        ObjectLocator locator = mockObjectLocator();
        Map<Class, Runnable> map = CollectionFactory.newMap();
        TypeCoercerProxy tc = mockTypeCoercerProxy();

        keyToContribution.put(Integer.class, def1);

        Class key = Integer.class;
        Runnable value = mockRunnable();

        expect(tc.coerce(value, Runnable.class)).andReturn(value);

        replay();

        MappedConfiguration<Class, Runnable> wrapper = new ValidatingMappedConfigurationWrapper<Class, Runnable>(
                Runnable.class, locator, tc, map, null, SERVICE_ID, def2, Class.class, keyToContribution);

        try
        {
            wrapper.add(key, value);
            unreachable();
        } catch (IllegalArgumentException ex)
        {
            assertMessageContains(ex, "Service contribution (to service 'Baz') conflicts with existing contribution");
        }

        verify();

        assertSame(keyToContribution.get(Integer.class), def1);
        assertTrue(map.isEmpty());
    }

    @Test
    public void null_key()
    {
        ContributionDef def = newContributionDef("contributionPlaceholder1");
        Map<Class, ContributionDef> keyToContribution = newMap();
        Runnable value = mockRunnable();
        ObjectLocator locator = mockObjectLocator();
        Map<Class, Runnable> map = CollectionFactory.newMap();

        replay();

        MappedConfiguration<Class, Runnable> wrapper = new ValidatingMappedConfigurationWrapper<Class, Runnable>(
                Runnable.class, locator, null, map, null, SERVICE_ID, def, Class.class, keyToContribution);

        try
        {
            wrapper.add(null, value);
            unreachable();
        } catch (NullPointerException ex)
        {
            assertEquals(ex.getMessage(), "Key for service contribution (to service 'Baz') was null.");
        }

        verify();

        assertTrue(map.isEmpty());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void wrong_key_type()
    {
        ContributionDef def = newContributionDef("contributionPlaceholder1");
        Map<?, ContributionDef> keyToContribution = CollectionFactory.newMap();
        Runnable value = mockRunnable();
        ObjectLocator locator = mockObjectLocator();
        Map<Class, Runnable> map = CollectionFactory.newMap();

        replay();

        MappedConfiguration wrapper = new ValidatingMappedConfigurationWrapper(Runnable.class, locator, null, map,
                null, SERVICE_ID, def, Class.class, keyToContribution);

        try
        {
            wrapper.add("java.util.List", value);
            unreachable();
        } catch (IllegalArgumentException ex)
        {
            assertEquals(
                    ex.getMessage(),
                    "Key for service contribution (to service 'Baz') was an instance of java.lang.String, but the expected key type was java.lang.Class.");
        }

        verify();

        assertTrue(map.isEmpty());
    }

    @Test
    public void null_value()
    {
        ContributionDef def = newContributionDef("contributionPlaceholder1");
        Map<Class, ContributionDef> keyToContribution = CollectionFactory.newMap();
        Map<Class, Runnable> map = CollectionFactory.newMap();
        ObjectLocator locator = mockObjectLocator();

        replay();

        MappedConfiguration<Class, Runnable> wrapper = new ValidatingMappedConfigurationWrapper<Class, Runnable>(
                Runnable.class, locator, null, map, null, SERVICE_ID, def, Class.class, keyToContribution);

        try
        {
            wrapper.add(Integer.class, null);
            unreachable();
        } catch (NullPointerException ex)
        {
            assertEquals(ex.getMessage(), "Service contribution (to service 'Baz') was null.");
        }

        verify();

        assertTrue(map.isEmpty());
    }

    private ContributionDef newContributionDef(String methodName)
    {
        return new ContributionDefImpl(SERVICE_ID, findMethod(methodName), false, getProxyFactory(), null, null);
    }

    public void contributionPlaceholder1()
    {

    }

    public void contributionPlaceholder2()
    {

    }
}
