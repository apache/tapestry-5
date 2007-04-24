// Copyright 2006 The Apache Software Foundation
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

package org.apache.tapestry.ioc;

import static org.apache.tapestry.ioc.internal.util.CollectionFactory.newList;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.tapestry.ioc.annotations.Contribute;
import org.apache.tapestry.ioc.annotations.Id;
import org.apache.tapestry.ioc.annotations.InjectService;
import org.apache.tapestry.ioc.annotations.Match;
import org.apache.tapestry.ioc.annotations.Order;
import org.apache.tapestry.ioc.annotations.Private;

/**
 * Module used to demonstrate decorator ordering.
 * 
 * 
 */
@Id("fred")
public class FredModule
{

    /**
     * Doesn't matter what the service does, we just want to verify that the decorators are invoked
     * in the order we expect.
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

    @Private
    public Runnable buildPrivateFred()
    {
        return buildFred();
    }

    /** A public alias of a private service. */
    public Runnable buildPrivateFredAlias(@InjectService("PrivateFred")
    Runnable privateFred)
    {
        return privateFred;
    }

    @Match("fred.*")
    @Order("before:Beta")
    public Object decorateAlpha(Object delegate, DecoratorList list)
    {
        list.add("alpha");

        return null;
    }

    @Match("fred.*")
    public Object decorateBeta(Object delegate, DecoratorList list)
    {
        list.add("beta");

        return null;
    }

    public NameListHolder buildUnorderedNames(Collection<String> configuration)
    {
        final List<String> sorted = newList(configuration);

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
    }

    @Contribute("UnorderedNames")
    public void contributeBeta(Configuration<String> configuration)
    {
        configuration.add("Beta");
    }

    @Private
    public NameListHolder buildPrivateUnorderedNames(final Collection<String> configuration)
    {
        return buildUnorderedNames(configuration);
    }

    public void contributePrivateUnorderedNames(Configuration<String> configuration)
    {
        configuration.add("PrivateUnorderedNames");
    }

    @Contribute("PrivateUnorderedNames")
    public void contributeOmega(Configuration<String> configuration)
    {
        configuration.add("Omega");
    }

    public NameListHolder buildPrivateUnorderedNamesAlias(@InjectService("PrivateUnorderedNames")
    NameListHolder privateService)
    {
        return privateService;
    }
}
