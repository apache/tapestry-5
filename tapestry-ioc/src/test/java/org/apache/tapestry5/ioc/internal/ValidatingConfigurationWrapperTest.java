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
import org.apache.tapestry5.ioc.ObjectLocator;
import org.apache.tapestry5.ioc.internal.util.CollectionFactory;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ValidatingConfigurationWrapperTest extends IOCInternalTestCase
{
    @SuppressWarnings("unchecked")
    @Test
    public void valid_contribution()
    {
        List<Runnable> collection = CollectionFactory.newList();
        Runnable value = mockRunnable();

        replay();

        Configuration wrapper = new ValidatingConfigurationWrapper(collection, "foo.Bar",
                                                                   Runnable.class, null);

        wrapper.add(value);

        verify();

        assertListsEquals(collection, value);
    }

    @SuppressWarnings({"unchecked"})
    @Test
    public void valid_class_contribution()
    {
        ObjectLocator locator = mockObjectLocator();
        final HashMap value = new HashMap();
        train_autobuild(locator, HashMap.class, value);
        List<Map> collection = CollectionFactory.newList();

        replay();

        Configuration wrapper = new ValidatingConfigurationWrapper(collection, "foo.Bar",
                                                                   Map.class, locator);

        wrapper.addInstance(HashMap.class);

        verify();

        assertListsEquals(collection, value);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void null_contribution()
    {
        List<Runnable> collection = CollectionFactory.newList();

        Configuration wrapper = new ValidatingConfigurationWrapper(collection, "Bar", Runnable.class,
                                                                   null);

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

    @SuppressWarnings("unchecked")
    @Test
    public void wrong_type_of_contribution()
    {
        List<Runnable> collection = CollectionFactory.newList();


        Configuration wrapper = new ValidatingConfigurationWrapper(collection, "Bar", Runnable.class,
                                                                   null);

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
    }
}
