// Copyright 2007, 2008, 2010 The Apache Software Foundation
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

import static org.apache.tapestry5.commons.util.CollectionFactory.newConcurrentMap;

import java.util.Map;

import org.apache.tapestry5.commons.ObjectLocator;
import org.apache.tapestry5.services.ApplicationStateContribution;
import org.apache.tapestry5.services.ApplicationStateCreator;
import org.apache.tapestry5.services.ApplicationStateManager;
import org.apache.tapestry5.services.ApplicationStatePersistenceStrategy;
import org.apache.tapestry5.services.ApplicationStatePersistenceStrategySource;

public class ApplicationStateManagerImpl implements ApplicationStateManager
{
    static final String DEFAULT_STRATEGY = "session";

    static class ApplicationStateAdapter<T>
    {
        private final Class<T> ssoClass;

        private final ApplicationStatePersistenceStrategy strategy;

        private final ApplicationStateCreator<T> creator;

        ApplicationStateAdapter(Class<T> ssoClass, ApplicationStatePersistenceStrategy strategy,
                ApplicationStateCreator<T> creator)
        {
            this.ssoClass = ssoClass;
            this.strategy = strategy;
            this.creator = creator;
        }

        T getOrCreate()
        {
            return strategy.get(ssoClass, creator);
        }

        void set(T sso)
        {
            strategy.set(ssoClass, sso);
        }

        boolean exists()
        {
            return strategy.exists(ssoClass);
        }

        T getIfExists()
        {
            return strategy.getIfExists(ssoClass);
        }
    }

    /**
     * The map will be extended periodically as new ASOs, not in the configuration, are encountered.
     * Thut is is thread
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
    private <T> ApplicationStateAdapter<T> newAdapter(final Class<T> ssoClass, String strategyName,
            ApplicationStateCreator<T> creator)
    {
        if (creator == null)
        {
            creator = new ApplicationStateCreator<T>()
            {
                public T create()
                {
                    return locator.autobuild("Instantiating instance of SSO class "
                            + ssoClass.getName(), ssoClass);
                }
            };
        }

        ApplicationStatePersistenceStrategy strategy = source.get(strategyName);

        return new ApplicationStateAdapter(ssoClass, strategy, creator);
    }

    @SuppressWarnings("unchecked")
    private <T> ApplicationStateAdapter<T> getAdapter(Class<T> ssoClass)
    {
        ApplicationStateAdapter<T> result = classToAdapter.get(ssoClass);

        // Not found is completely OK, we'll define it on the fly.

        if (result == null)
        {
            result = newAdapter(ssoClass, DEFAULT_STRATEGY, null);
            classToAdapter.put(ssoClass, result);
        }

        return result;
    }

    public <T> T get(Class<T> ssoClass)
    {
        return getAdapter(ssoClass).getOrCreate();
    }

    public <T> T getIfExists(Class<T> ssoClass)
    {
        return getAdapter(ssoClass).getIfExists();
    }

    public <T> void set(Class<T> ssoClass, T sso)
    {
        getAdapter(ssoClass).set(sso);
    }

    public <T> boolean exists(Class<T> ssoClass)
    {
        return getAdapter(ssoClass).exists();
    }

}
