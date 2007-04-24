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

package org.apache.tapestry.internal.services;

import static org.apache.tapestry.ioc.internal.util.CollectionFactory.newCaseInsensitiveMap;

import java.util.Collection;
import java.util.Map;

import org.apache.tapestry.ComponentResources;
import org.apache.tapestry.ioc.internal.util.CollectionFactory;
import org.apache.tapestry.ioc.internal.util.InternalUtils;
import org.apache.tapestry.model.ComponentModel;
import org.apache.tapestry.services.MetaDataLocator;
import org.apache.tapestry.services.PersistentFieldBundle;
import org.apache.tapestry.services.PersistentFieldChange;
import org.apache.tapestry.services.PersistentFieldManager;
import org.apache.tapestry.services.PersistentFieldStrategy;

public class PersistentFieldManagerImpl implements PersistentFieldManager
{
    public static final String META_KEY = "tapestry.persistence-strategy";

    public static final String DEFAULT_STRATEGY = "session";

    private final MetaDataLocator _metaDataLocator;

    private final Map<String, PersistentFieldStrategy> _strategies;

    public PersistentFieldManagerImpl(MetaDataLocator locator,
            Map<String, PersistentFieldStrategy> strategies)
    {
        _metaDataLocator = locator;

        _strategies = newCaseInsensitiveMap(strategies);
    }

    private PersistentFieldStrategy getStrategy(String strategyName)
    {
        PersistentFieldStrategy result = _strategies.get(strategyName);

        if (result == null)
            throw new RuntimeException(ServicesMessages.unknownPersistentFieldStrategy(
                    strategyName,
                    _strategies.keySet()));

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

        return _metaDataLocator.findMeta(META_KEY, resources);
    }
}
