// Copyright 2006, 2007, 2008 The Apache Software Foundation
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

import org.apache.tapestry5.ComponentResources;
import org.apache.tapestry5.SymbolConstants;
import org.apache.tapestry5.ioc.internal.util.CollectionFactory;
import org.apache.tapestry5.ioc.internal.util.InternalUtils;
import org.apache.tapestry5.model.ComponentModel;
import org.apache.tapestry5.services.MetaDataLocator;
import org.apache.tapestry5.services.PersistentFieldBundle;
import org.apache.tapestry5.services.PersistentFieldChange;
import org.apache.tapestry5.services.PersistentFieldStrategy;

import java.util.Collection;
import java.util.Map;

public class PersistentFieldManagerImpl implements PersistentFieldManager
{
    private final MetaDataLocator metaDataLocator;

    private final Map<String, PersistentFieldStrategy> strategies;

    public PersistentFieldManagerImpl(MetaDataLocator locator,
                                      Map<String, PersistentFieldStrategy> strategies)
    {
        metaDataLocator = locator;

        this.strategies = strategies;
    }

    private PersistentFieldStrategy getStrategy(String strategyName)
    {
        PersistentFieldStrategy result = strategies.get(strategyName);

        if (result == null)
            throw new RuntimeException(ServicesMessages.unknownPersistentFieldStrategy(
                    strategyName,
                    strategies.keySet()));

        return result;
    }

    public PersistentFieldBundle gatherChanges(String pageName)
    {
        Collection<PersistentFieldChange> allChanges = CollectionFactory.newList();

        for (PersistentFieldStrategy strategy : strategies.values())
        {
            allChanges.addAll(strategy.gatherFieldChanges(pageName));
        }

        return new PersistentFieldBundleImpl(allChanges);
    }

    public void discardChanges(String pageName)
    {
        for (PersistentFieldStrategy strategy : strategies.values())
        {
            strategy.discardChanges(pageName);
        }
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

        if (InternalUtils.isNonBlank(strategy)) return strategy;

        return metaDataLocator.findMeta(SymbolConstants.PERSISTENCE_STRATEGY, resources, String.class);
    }
}
