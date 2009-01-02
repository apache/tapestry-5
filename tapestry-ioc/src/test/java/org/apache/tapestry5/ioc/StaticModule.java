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

import org.apache.tapestry5.ioc.internal.util.CollectionFactory;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class StaticModule
{
    private static boolean instantiated;

    private static boolean fredRan;

    private static boolean decoratorRan;

    public StaticModule()
    {
        setInstantiated(true);
    }

    public static Runnable buildFred()
    {
        return new Runnable()
        {
            public void run()
            {
                setFredRan(true);
            }
        };
    }

    public static Runnable buildBarney()
    {
        return new Runnable()
        {
            public void run()
            {
            }
        };
    }

    public static Runnable decorateBarney(final Object delegate)
    {
        return new Runnable()
        {
            public void run()
            {
                setDecoratorRan(true);

                ((Runnable) delegate).run();
            }
        };
    }

    static synchronized void setFredRan(boolean fredRan)
    {
        StaticModule.fredRan = fredRan;
    }

    static synchronized boolean getFredRan()
    {
        return fredRan;
    }

    static synchronized void setInstantiated(boolean instantiated)
    {
        StaticModule.instantiated = instantiated;
    }

    static synchronized boolean isInstantiated()
    {
        return instantiated;
    }

    static synchronized void setDecoratorRan(boolean decoratorRan)
    {
        StaticModule.decoratorRan = decoratorRan;
    }

    static synchronized boolean getDecoratorRan()
    {
        return decoratorRan;
    }

    public static NameListHolder buildNames(final Collection<String> configuration)
    {
        return new NameListHolder()
        {
            public List<String> getNames()
            {
                List<String> result = CollectionFactory.newList(configuration);

                Collections.sort(result);

                return result;
            }
        };
    }

    public static void contributeNames(Configuration<String> configuration)
    {
        configuration.add("Fred");
    }
}
