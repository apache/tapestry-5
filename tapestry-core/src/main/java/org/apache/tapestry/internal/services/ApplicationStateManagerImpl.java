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

package org.apache.tapestry.internal.services;

import static org.apache.tapestry.ioc.internal.util.CollectionFactory.newConcurrentMap;
import org.apache.tapestry.services.*;

import java.util.Map;

public class ApplicationStateManagerImpl implements ApplicationStateManager
{
    static final String DEFAULT_STRATEGY = "session";

    static class ApplicationStateAdapter<T>
    {
        private final Class<T> _asoClass;

        private final ApplicationStatePersistenceStrategy _strategy;

        private final ApplicationStateCreator<T> _creator;

        ApplicationStateAdapter(Class<T> asoClass, ApplicationStatePersistenceStrategy strategy,
                                ApplicationStateCreator<T> creator)
        {
            _asoClass = asoClass;
            _strategy = strategy;
            _creator = creator;
        }

        T getOrCreate()
        {
            return _strategy.get(_asoClass, _creator);
        }

        void set(T aso)
        {
            _strategy.set(_asoClass, aso);
        }

        boolean exists()
        {
            return _strategy.exists(_asoClass);
        }
    }

    /**
     * The map will be extended periodically as new ASOs, not in the configuration, are encountered. Thut is is thread
     * safe.
     */
    private final Map<Class, ApplicationStateAdapter> _classToAdapter = newConcurrentMap();

    private final ApplicationStatePersistenceStrategySource _source;

    @SuppressWarnings("unchecked")
    public ApplicationStateManagerImpl(Map<Class, ApplicationStateContribution> configuration,
                                       ApplicationStatePersistenceStrategySource source)
    {
        _source = source;

        for (Class asoClass : configuration.keySet())
        {
            ApplicationStateContribution contribution = configuration.get(asoClass);

            ApplicationStateAdapter adapter = newAdapter(asoClass, contribution.getStrategy(),
                                                         contribution.getCreator());

            _classToAdapter.put(asoClass, adapter);
        }

    }

    @SuppressWarnings("unchecked")
    private <T> ApplicationStateAdapter<T> newAdapter(final Class<T> asoClass, String strategyName,
                                                      ApplicationStateCreator<T> creator)
    {
        if (creator == null) creator = new ApplicationStateCreator<T>()
        {
            public T create()
            {
                try
                {
                    return asoClass.newInstance();
                }
                catch (Exception ex)
                {
                    throw new RuntimeException(ex);
                }
            }

        };

        ApplicationStatePersistenceStrategy strategy = _source.get(strategyName);

        return new ApplicationStateAdapter(asoClass, strategy, creator);
    }

    @SuppressWarnings("unchecked")
    private <T> ApplicationStateAdapter<T> getAdapter(Class<T> asoClass)
    {
        ApplicationStateAdapter<T> result = _classToAdapter.get(asoClass);

        // Not found is completely OK, we'll define it on the fly.

        if (result == null)
        {
            result = newAdapter(asoClass, DEFAULT_STRATEGY, null);
            _classToAdapter.put(asoClass, result);
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
