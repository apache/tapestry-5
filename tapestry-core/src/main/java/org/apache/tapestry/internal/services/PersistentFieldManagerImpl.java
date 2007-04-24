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

package org.apache.tapestry.internal.services;

import java.util.Collection;
import java.util.Map;

import org.apache.tapestry.ComponentResources;
import org.apache.tapestry.ioc.internal.util.CollectionFactory;
import org.apache.tapestry.ioc.internal.util.InternalUtils;
import org.apache.tapestry.model.ComponentModel;
import org.apache.tapestry.services.PersistentFieldBundle;
import org.apache.tapestry.services.PersistentFieldChange;
import org.apache.tapestry.services.PersistentFieldManager;
import org.apache.tapestry.services.PersistentFieldStrategy;

public class PersistentFieldManagerImpl implements PersistentFieldManager
{
    static final String META_KEY = "tapestry.persistence-strategy";

    static final String DEFAULT_STRATEGY = "session";

    private final Map<String, PersistentFieldStrategy> _strategies;

    public PersistentFieldManagerImpl(final Map<String, PersistentFieldStrategy> strategies)
    {
        _strategies = strategies;
    }

    private PersistentFieldStrategy getStrategy(String strategyName)
    {
        PersistentFieldStrategy result = _strategies.get(strategyName);

        if (result == null)
        {
            String catalog = InternalUtils.joinSorted(_strategies.keySet());

            throw new RuntimeException(ServicesMessages.unknownPersistentFieldStrategy(
                    strategyName,
                    catalog));
        }

        return result;
    }

    public PersistentFieldBundle gatherChanges(String pageName)
    {
        Collection<PersistentFieldChange> allChanges = CollectionFactory.newList();

        for (PersistentFieldStrategy strategy : _strategies.values())
        {
            allChanges.addAll(strategy.gatherFieldChanges(pageName));
        }

        return new PersistentFieldBundleImpl(allChanges);
    }

    public void postChange(String pageName, ComponentResources resources, String fieldName,
            Object newValue)
    {
        String strategyName = findStrategy(resources, fieldName);
        PersistentFieldStrategy strategy = getStrategy(strategyName);

        strategy.postChange(pageName, resources.getNestedId(), fieldName, newValue);
    }

    private String findStrategy(ComponentResources resources, String fieldName)
    {
        ComponentModel model = resources.getComponentModel();

        String strategy = model.getFieldPersistenceStrategy(fieldName);

        if (InternalUtils.isNonBlank(strategy))
            return strategy;

        // OK, it isn't specified for the field itself, so work up the component hierarchy,
        // checking to see if any component set a default persistent strategy.

        ComponentResources search = resources;
        while (search != null)
        {
            model = search.getComponentModel();

            strategy = model.getMeta(META_KEY);

            if (strategy != null)
                return strategy;

            // Work up the containment hierarchy. For a persistent field inside a mixin, the
            // container is the component. From there we work up the normal page containment
            // hierarchy.

            search = search.getContainerResources();
        }

        return DEFAULT_STRATEGY;
    }
}
