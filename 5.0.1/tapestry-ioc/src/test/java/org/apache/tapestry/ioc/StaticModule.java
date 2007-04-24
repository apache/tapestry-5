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

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.tapestry.ioc.annotations.Id;
import org.apache.tapestry.ioc.internal.util.CollectionFactory;

/**
 * 
 */
@Id("static")
public class StaticModule
{
    private static boolean _instantiated;

    private static boolean _fredRan;

    private static boolean _decoratorRan;

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

    public static synchronized void setFredRan(boolean fredRan)
    {
        _fredRan = fredRan;
    }

    public static synchronized boolean getFredRan()
    {
        return _fredRan;
    }

    public static synchronized void setInstantiated(boolean instantiated)
    {
        _instantiated = instantiated;
    }

    public static synchronized boolean isInstantiated()
    {
        return _instantiated;
    }

    public static synchronized void setDecoratorRan(boolean decoratorRan)
    {
        _decoratorRan = decoratorRan;
    }

    public static synchronized boolean getDecoratorRan()
    {
        return _decoratorRan;
    }

    public static NameListHolder buildNames(final Collection<String> configuration)
    {
        return new NameListHolder()
        {
            public List<String> getNames()
            {
                List result = CollectionFactory.newList(configuration);

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
