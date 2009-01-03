// Copyright 2006, 2007, 2008, 2009 The Apache Software Foundation
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

import org.apache.tapestry5.ioc.ObjectLocator;
import org.apache.tapestry5.ioc.OrderedConfiguration;
import org.apache.tapestry5.ioc.internal.util.Orderer;
import org.slf4j.Logger;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

public class ValidatingOrderedConfigurationWrapperTest extends IOCInternalTestCase
{
    @Test
    public void valid_type_long_form()
    {
        Runnable contribution = mockRunnable();
        Runnable pre = mockRunnable();
        Runnable post = mockRunnable();
        Logger logger = mockLogger();
        Orderer<Runnable> orderer = new Orderer<Runnable>(logger);


        orderer.add("pre", pre);
        orderer.add("post", post);

        replay();

        OrderedConfiguration<Runnable> wrapper = new ValidatingOrderedConfigurationWrapper<Runnable>(
                orderer, null, null, "Service", Runnable.class, null);

        wrapper.add("id", contribution, "after:pre", "before:post");

        verify();

        assertListsEquals(orderer.getOrdered(), pre, contribution, post);
    }

    @Test
    public void contribute_valid_class()
    {
        Logger logger = mockLogger();
        Orderer<Map> orderer = new Orderer<Map>(logger);
        Map pre = new HashMap();
        Map post = new HashMap();
        HashMap contribution = new HashMap();
        ObjectLocator locator = mockObjectLocator();

        train_autobuild(locator, HashMap.class, contribution);

        orderer.add("pre", pre);
        orderer.add("post", post);

        replay();

        OrderedConfiguration<Map> wrapper = new ValidatingOrderedConfigurationWrapper<Map>(
                orderer, null, null, "Service", Map.class, locator);

        wrapper.addInstance("id", HashMap.class, "after:pre", "before:post");

        verify();

        assertListsEquals(orderer.getOrdered(), pre, contribution, post);
    }

    @Test
    public void null_object_passed_through()
    {
        Logger logger = mockLogger();
        Orderer<Runnable> orderer = new Orderer<Runnable>(logger);

        replay();

        OrderedConfiguration<Runnable> wrapper = new ValidatingOrderedConfigurationWrapper<Runnable>(
                orderer, null, null, "Service", Runnable.class, null);

        wrapper.add("id", null);

        verify();

        assertTrue(orderer.getOrdered().isEmpty());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void incorrect_contribution_type_is_passed_through_as_null()
    {
        Logger logger = mockLogger();
        Orderer<Runnable> orderer = new Orderer<Runnable>(logger);

        replay();

        OrderedConfiguration wrapper = new ValidatingOrderedConfigurationWrapper(orderer, null, null, "Service",
                                                                                 Runnable.class, null);

        try
        {
            wrapper.add("id", "string");
            unreachable();
        }
        catch (IllegalArgumentException ex)
        {
            assertEquals(ex.getMessage(),
                         "Service contribution (to service 'Service') was an instance of java.lang.String, which is not assignable to the configuration type java.lang.Runnable.");
        }

        verify();
    }
}
