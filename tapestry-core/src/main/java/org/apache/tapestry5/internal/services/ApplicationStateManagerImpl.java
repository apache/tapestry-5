// Copyright 2007, 2008 The Apache Software Foundation
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

package org.apache.tapestry5.internal.services;

import org.apache.tapestry5.ioc.ObjectLocator;
import static org.apache.tapestry5.ioc.internal.util.CollectionFactory.newConcurrentMap;
import org.apache.tapestry5.services.*;

import java.util.Map;

public class ApplicationStateManagerImpl implements ApplicationStateManager
{
    static final String DEFAULT_STRATEGY = "session";

    static class ApplicationStateAdapter<T>
    {
        private final Class<T> asoClass;

        private final ApplicationStatePersistenceStrategy strategy;

        private final ApplicationStateCreator<T> creator;

        ApplicationStateAdapter(Class<T> asoClass, ApplicationStatePersistenceStrategy strategy,
                                ApplicationStateCreator<T> creator)
        {
            this.asoClass = asoClass;
            this.strategy = strategy;
            this.creator = creator;
        }

        T getOrCreate()
        {
            return strategy.get(asoClass, creator);
        }

        void set(T aso)
        {
            strategy.set(asoClass, aso);
        }

        boolean exists()
        {
            return strategy.exists(asoClass);
        }
    }

    /**
     * The map will be extended periodically as new ASOs, not in the configuration, are encountered. Thut is is thread
     * safe.
     */
    private final Map<Class, ApplicationStateAdapter> classToAdapter = newConcurrentMap();

    private final ApplicationStatePersistenceStrategySource source;

    private final ObjectLocator locator;

    @SuppressWarnings("unchecked")
    public ApplicationStateManagerImpl(Map<Class, ApplicationStateContribution> configuration,
                                       ApplicationStatePersistenceStrategySource source, ObjectLocator locator)
    {
        this.source = source;
        this.locator = locator;

        for (Class asoClass : configuration.keySet())
        {
            ApplicationStateContribution contribution = configuration.get(asoClass);

            ApplicationStateAdapter adapter = newAdapter(asoClass, contribution.getStrategy(),
                                                         contribution.getCreator());

            classToAdapter.put(asoClass, adapter);
        }

    }

    @SuppressWarnings("unchecked")
    private <T> ApplicationStateAdapter<T> newAdapter(final Class<T> asoClass, String strategyName,
                                                      ApplicationStateCreator<T> creator)
    {
        if (creator == null)
        {
            creator = new ApplicationStateCreator<T>()
            {
                public T create()
                {
                    return locator.autobuild(asoClass);
                }
            };
        }

        ApplicationStatePersistenceStrategy strategy = source.get(strategyName);

        return new ApplicationStateAdapter(asoClass, strategy, creator);
    }

    @SuppressWarnings("unchecked")
    private <T> ApplicationStateAdapter<T> getAdapter(Class<T> asoClass)
    {
        ApplicationStateAdapter<T> result = classToAdapter.get(asoClass);

        // Not found is completely OK, we'll define it on the fly.

        if (result == null)
        {
            result = newAdapter(asoClass, DEFAULT_STRATEGY, null);
            classToAdapter.put(asoClass, result);
        }

        return result;
    }

    public <T> T get(Class<T> asoClass)
    {
        return getAdapter(asoClass).getOrCreate();
    }

    public <T> T getIfExists(Class<T> asoClass)
    {
        ApplicationStateAdapter<T> adapter = getAdapter(asoClass);

        return adapter.exists() ? adapter.getOrCreate() : null;
    }

    public <T> void set(Class<T> asoClass, T aso)
    {
        getAdapter(asoClass).set(aso);
    }

    public <T> boolean exists(Class<T> asoClass)
    {
        return getAdapter(asoClass).exists();
    }

}
