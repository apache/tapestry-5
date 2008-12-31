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
import org.apache.tapestry5.PersistenceConstants;
import org.apache.tapestry5.SymbolConstants;
import org.apache.tapestry5.internal.test.InternalBaseTestCase;
import static org.apache.tapestry5.ioc.internal.util.CollectionFactory.newList;
import static org.apache.tapestry5.ioc.internal.util.CollectionFactory.newMap;
import org.apache.tapestry5.model.ComponentModel;
import org.apache.tapestry5.services.MetaDataLocator;
import org.apache.tapestry5.services.PersistentFieldBundle;
import org.apache.tapestry5.services.PersistentFieldChange;
import org.apache.tapestry5.services.PersistentFieldStrategy;
import org.testng.annotations.Test;

import java.util.Collection;
import java.util.Map;

public class PersistentFieldManagerImplTest extends InternalBaseTestCase
{
    @Test
    public void post_change_with_unknown_strategy()
    {
        String fieldName = "field";

        PersistentFieldStrategy strat1 = newPersistentFieldStrategy();
        PersistentFieldStrategy strat2 = newPersistentFieldStrategy();
        ComponentResources resources = mockComponentResources();
        ComponentModel model = mockComponentModel();

        Map<String, PersistentFieldStrategy> strategies = newMap();
        strategies.put("foo", strat1);
        strategies.put("bar", strat2);

        train_getComponentModel(resources, model);

        train_getFieldPersistenceStrategy(model, fieldName, "braveheart");

        replay();

        PersistentFieldManager manager = new PersistentFieldManagerImpl(null, strategies);

        try
        {
            manager.postChange("foo.Bar", resources, fieldName, null);
            unreachable();
        }
        catch (RuntimeException ex)
        {
            assertEquals(
                    ex.getMessage(),
                    "\'braveheart\' is not a defined persistent strategy.  Defined strategies: bar, foo.");
        }

        verify();
    }

    /**
     * TAPESTRY-1475
     */
    @Test
    public void discard_changes()
    {
        PersistentFieldStrategy strat1 = newPersistentFieldStrategy();
        PersistentFieldStrategy strat2 = newPersistentFieldStrategy();

        Map<String, PersistentFieldStrategy> strategies = newMap();
        strategies.put("foo", strat1);
        strategies.put("bar", strat2);

        String pageName = "gnip.gnop";

        strat1.discardChanges(pageName);
        strat2.discardChanges(pageName);

        replay();

        PersistentFieldManager manager = new PersistentFieldManagerImpl(null, strategies);

        manager.discardChanges(pageName);

        verify();
    }

    @Test
    public void post_change()
    {
        String pageName = "foo.Bar";
        String nestedId = "nested";
        String fieldName = "field";
        String strategyName = "foo";

        ComponentResources resources = mockComponentResources();
        ComponentModel model = mockComponentModel();
        PersistentFieldStrategy strat = newPersistentFieldStrategy();
        Object value = new Object();

        Map<String, PersistentFieldStrategy> strategies = newMap();
        strategies.put(strategyName, strat);

        train_getComponentModel(resources, model);

        train_getFieldPersistenceStrategy(model, fieldName, strategyName);

        train_getNestedId(resources, nestedId);

        strat.postChange(pageName, nestedId, fieldName, value);

        replay();

        PersistentFieldManager manager = new PersistentFieldManagerImpl(null, strategies);

        manager.postChange(pageName, resources, fieldName, value);

        verify();
    }

    public void strategy_name_is_case_insensitive()
    {
        String pageName = "foo.Bar";
        String nestedId = "nested";
        String fieldName = "field";
        String strategyName = "FOO";

        ComponentResources resources = mockComponentResources();
        ComponentModel model = mockComponentModel();
        PersistentFieldStrategy strat = newPersistentFieldStrategy();
        Object value = new Object();

        Map<String, PersistentFieldStrategy> strategies = newMap();
        strategies.put("foo", strat);

        train_getComponentModel(resources, model);

        train_getFieldPersistenceStrategy(model, fieldName, strategyName);

        train_getNestedId(resources, nestedId);

        strat.postChange(pageName, nestedId, fieldName, value);

        replay();

        PersistentFieldManager manager = new PersistentFieldManagerImpl(null, strategies);

        manager.postChange(pageName, resources, fieldName, value);

        verify();
    }

    @Test
    public void post_change_strategy_by_meta_data()
    {
        String pageName = "foo.Bar";
        String nestedId = "nested";
        String fieldName = "field";
        String strategyName = "foo";

        ComponentResources resources = mockComponentResources();
        ComponentModel model = mockComponentModel();
        PersistentFieldStrategy strat = newPersistentFieldStrategy();
        MetaDataLocator locator = mockMetaDataLocator();

        Object value = new Object();

        Map<String, PersistentFieldStrategy> strategies = newMap();
        strategies.put(strategyName, strat);

        train_getComponentModel(resources, model);

        train_getFieldPersistenceStrategy(model, fieldName, "");

        train_findMeta(locator, SymbolConstants.PERSISTENCE_STRATEGY, resources, String.class, strategyName);

        train_getNestedId(resources, nestedId);

        strat.postChange(pageName, nestedId, fieldName, value);

        replay();

        PersistentFieldManager manager = new PersistentFieldManagerImpl(locator, strategies);

        manager.postChange(pageName, resources, fieldName, value);

        verify();
    }

    @Test
    public void post_change_with_ultimate_default_strategy()
    {
        String pageName = "foo.Bar";
        String nestedId = "nested";
        String fieldName = "field";

        ComponentResources resources = mockComponentResources();
        ComponentModel model = mockComponentModel();
        MetaDataLocator locator = mockMetaDataLocator();

        PersistentFieldStrategy strat = newPersistentFieldStrategy();
        Object value = new Object();

        Map<String, PersistentFieldStrategy> strategies = newMap();
        strategies.put(PersistenceConstants.SESSION, strat);

        train_getComponentModel(resources, model);

        train_getFieldPersistenceStrategy(model, fieldName, "");

        train_findMeta(
                locator,
                SymbolConstants.PERSISTENCE_STRATEGY,
                resources, String.class,
                PersistenceConstants.SESSION);

        train_getNestedId(resources, nestedId);

        strat.postChange(pageName, nestedId, fieldName, value);

        replay();

        PersistentFieldManager manager = new PersistentFieldManagerImpl(locator, strategies);

        manager.postChange(pageName, resources, fieldName, value);

        verify();
    }

    protected final PersistentFieldStrategy newPersistentFieldStrategy()
    {
        return newMock(PersistentFieldStrategy.class);
    }

    @Test
    public void gather_changes()
    {
        Object value1 = new Object();
        Object value2 = new Object();

        PersistentFieldStrategy strat1 = newPersistentFieldStrategy();

        Collection<PersistentFieldChange> changes1 = newList();
        changes1.add(new PersistentFieldChangeImpl("component", "field1", value1));

        PersistentFieldStrategy strat2 = newPersistentFieldStrategy();

        Collection<PersistentFieldChange> changes2 = newList();
        changes2.add(new PersistentFieldChangeImpl("component", "field2", value2));

        // We don't know the exact order the strategies will be ordered in the map,
        // so we can't guarantee the order the strategies will be invoked.

        getMocksControl().checkOrder(false);

        expect(strat1.gatherFieldChanges("foo.Bar")).andReturn(changes1);

        expect(strat2.gatherFieldChanges("foo.Bar")).andReturn(changes2);

        replay();

        Map<String, PersistentFieldStrategy> strategies = newMap();

        strategies.put("alpha", strat1);
        strategies.put("beta", strat2);

        PersistentFieldManager manager = new PersistentFieldManagerImpl(null, strategies);

        PersistentFieldBundle bundle = manager.gatherChanges("foo.Bar");

        assertSame(bundle.getValue("component", "field1"), value1);
        assertSame(bundle.getValue("component", "field2"), value2);

        verify();
    }
}
