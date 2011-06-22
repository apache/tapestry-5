// Copyright 2006, 2007, 2009 The Apache Software Foundation
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

package org.apache.tapestry5.ioc;

import org.apache.tapestry5.ioc.annotations.Match;
import org.apache.tapestry5.ioc.annotations.Order;
import org.apache.tapestry5.ioc.internal.util.CollectionFactory;
import org.apache.tapestry5.ioc.internal.util.InternalUtils;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Module used to demonstrate decorator ordering.
 */
public class FredModule
{

    /**
     * Doesn't matter what the service does, we just want to verify that the decorators are invoked in the order we
     * expect.
     */
    public Runnable buildFred()
    {
        return new Runnable()
        {
            public void run()
            {
            }
        };
    }

    @Match(
            {"UnorderedNames", "Fred"})
    @Order("before:Beta")
    public Object decorateAlpha(Object delegate, DecoratorList list)
    {
        list.add("alpha");

        return null;
    }

    @Match(
            {"UnorderedNames", "Fred"})
    public Object decorateBeta(Object delegate, DecoratorList list)
    {
        list.add("beta");

        return null;
    }

    public NameListHolder buildUnorderedNames(Collection<String> configuration)
    {
        final List<String> sorted = CollectionFactory.newList(configuration);

        Collections.sort(sorted);

        return new NameListHolder()
        {

            public List<String> getNames()
            {
                return sorted;
            }
        };
    }

    public NameListHolder buildOrderedNames(final List<String> configuration)
    {
        return new NameListHolder()
        {

            public List<String> getNames()
            {
                return configuration;
            }
        };
    }

    public void contributeOrderedNames(OrderedConfiguration<String> configuration)
    {
        // Order "FRED" after "BARNEY"

        configuration.add("fred", "FRED", "after:barney");
        configuration.add("barney", "BARNEY");
    }

    public void contributeUnorderedNames(Configuration<String> configuration)
    {
        configuration.add("UnorderedNames");
        configuration.add("Beta");
    }

    public StringLookup buildStringLookup(final Map<String, String> configuration)
    {
        return new StringLookup()
        {
            public String lookup(String key)
            {
                return configuration.get(key);
            }

            public List<String> keys()
            {
                return InternalUtils.sortedKeys(configuration);
            }
        };
    }

    public void contributeStringLookup(MappedConfiguration<String, String> configuration)
    {
        configuration.add("fred", "FRED");
        configuration.add("wilma", "WILMA");
    }
}
