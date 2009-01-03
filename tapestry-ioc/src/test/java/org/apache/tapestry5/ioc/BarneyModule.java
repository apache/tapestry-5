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

package org.apache.tapestry5.ioc;

import org.apache.tapestry5.ioc.annotations.Match;
import org.apache.tapestry5.ioc.annotations.Order;
import org.apache.tapestry5.ioc.internal.util.CollectionFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Module used to demonstrate decorator ordering.
 */

public class BarneyModule
{
    @Match(
            {"UnorderedNames", "Fred", "PrivateFredAlias"})
    @Order("after:Beta")
    public Object decorateGamma(Object delegate, DecoratorList list)
    {
        list.add("gamma");

        return null;
    }

    public Sizer buildSizer(final Map<Class, Sizer> configuration)
    {
        return new Sizer()
        {
            public int size(Object object)
            {
                if (object == null) return 0;

                Sizer sizer = configuration.get(object.getClass());

                if (sizer != null) return sizer.size(object);

                return 1;
            }
        };
    }

    public void contributeSizer(MappedConfiguration<Class, Sizer> configuration)
    {
        Sizer listSizer = new Sizer()
        {
            public int size(Object object)
            {
                List list = (List) object;

                return list.size();
            }
        };

        Sizer mapSizer = new Sizer()
        {
            public int size(Object object)
            {
                Map map = (Map) object;

                return map.size();
            }
        };

        // Have to work on concrete class, rather than type, until we move the StrategyFactory
        // over from HiveMind.

        configuration.add(ArrayList.class, listSizer);
        configuration.add(HashMap.class, mapSizer);
    }

    /**
     * Put DecoratorList in module barney, where so it won't accidentally be decorated (which recusively builds the
     * service, and is caught as a failure).
     */
    public DecoratorList buildDecoratorList()
    {
        return new DecoratorList()
        {
            private List<String> names = CollectionFactory.newList();

            public void add(String name)
            {
                names.add(name);
            }

            public List<String> getNames()
            {
                return names;
            }
        };
    }

    public void contributeUnorderedNames(Configuration<String> configuration)
    {
        configuration.add("Gamma");
    }

    public void contributeStringLookup(MappedConfiguration<String, String> configuration)
    {
        configuration.add("barney", "BARNEY");
        configuration.add("betty", "BETTY");
    }
}
