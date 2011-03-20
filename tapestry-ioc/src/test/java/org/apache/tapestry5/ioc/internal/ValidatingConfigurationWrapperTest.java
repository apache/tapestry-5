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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.tapestry5.ioc.Configuration;
import org.apache.tapestry5.ioc.ObjectLocator;
import org.apache.tapestry5.ioc.internal.util.CollectionFactory;
import org.testng.annotations.Test;

@SuppressWarnings(
{ "rawtypes", "unchecked" })
public class ValidatingConfigurationWrapperTest extends IOCInternalTestCase
{
    @Test
    public void valid_contribution()
    {
        List<Runnable> collection = CollectionFactory.newList();
        Runnable value = mockRunnable();
        TypeCoercerProxy tc = mockTypeCoercerProxy();

        expect(tc.coerce(value, Runnable.class)).andReturn(value);

        replay();

        Configuration wrapper = new ValidatingConfigurationWrapper(Runnable.class, null, tc, collection, "foo.Bar");

        wrapper.add(value);

        verify();

        assertListsEquals(collection, value);
    }

    @Test
    public void coerced_contribution()
    {
        List<Runnable> collection = CollectionFactory.newList();
        Runnable value = mockRunnable();
        TypeCoercerProxy tc = mockTypeCoercerProxy();
        String contributed = "coerceme";

        expect(tc.coerce(contributed, Runnable.class)).andReturn(value);

        replay();

        Configuration wrapper = new ValidatingConfigurationWrapper(Runnable.class, null, tc, collection, "foo.Bar");

        wrapper.add(contributed);

        verify();

        assertListsEquals(collection, value);
    }

    @Test
    public void valid_class_contribution()
    {
        ObjectLocator locator = mockObjectLocator();
        HashMap contributedValue = new HashMap();
        train_autobuild(locator, HashMap.class, contributedValue);
        List<Map> collection = CollectionFactory.newList();
        TypeCoercerProxy tc = mockTypeCoercerProxy();

        expect(tc.coerce(contributedValue, Map.class)).andReturn(contributedValue);

        replay();

        Configuration wrapper = new ValidatingConfigurationWrapper(Map.class, locator, tc, collection, "foo.Bar");

        wrapper.addInstance(HashMap.class);

        verify();

        assertListsEquals(collection, contributedValue);
    }

    @Test
    public void null_contribution()
    {
        List<Runnable> collection = CollectionFactory.newList();

        Configuration wrapper = new ValidatingConfigurationWrapper(Runnable.class, null, null, collection, "Bar");

        try
        {
            wrapper.add(null);
            unreachable();
        }
        catch (NullPointerException ex)
        {
            assertEquals(ex.getMessage(), "Service contribution (to service 'Bar') was null.");
        }
    }

    @Test
    public void wrong_type_of_contribution()
    {
        List<Runnable> collection = CollectionFactory.newList();
        Throwable e = new RuntimeException("No go");
        TypeCoercerProxy tc = mockTypeCoercerProxy();
        String contributedValue = "runnable";

        expect(tc.coerce(contributedValue, Runnable.class)).andThrow(e);

        Configuration wrapper = new ValidatingConfigurationWrapper(Runnable.class, null, tc, collection, "Bar");

        replay();

        try
        {
            wrapper.add(contributedValue);
            unreachable();
        }
        catch (RuntimeException ex)
        {
            assertSame(ex, e);
        }

        verify();
    }
}
