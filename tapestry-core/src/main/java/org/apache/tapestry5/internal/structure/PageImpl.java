// Copyright 2006, 2007, 2008, 2009, 2010, 2011 The Apache Software Foundation
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.apache.tapestry5.internal.structure;

import org.apache.tapestry5.ComponentResources;
import org.apache.tapestry5.internal.services.PersistentFieldManager;
import org.apache.tapestry5.ioc.internal.util.CollectionFactory;
import org.apache.tapestry5.ioc.internal.util.OneShotLock;
import org.apache.tapestry5.ioc.services.PerThreadValue;
import org.apache.tapestry5.ioc.services.PerthreadManager;
import org.apache.tapestry5.runtime.Component;
import org.apache.tapestry5.runtime.PageLifecycleListener;
import org.apache.tapestry5.services.PersistentFieldBundle;
import org.apache.tapestry5.services.pageload.ComponentResourceSelector;
import org.slf4j.Logger;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

public class PageImpl implements Page
{
    private final String name;

    private final ComponentResourceSelector selector;

    private final PersistentFieldManager persistentFieldManager;

    private ComponentPageElement rootElement;

    private final List<PageLifecycleListener> lifecycleListeners = CollectionFactory.newThreadSafeList();

    private final List<PageResetListener> resetListeners = CollectionFactory.newList();

    private boolean loadComplete;

    private final OneShotLock lock = new OneShotLock();

    private final Map<String, ComponentPageElement> idToComponent = CollectionFactory.newCaseInsensitiveMap();

    // TODO: loadComplete, assemblyTime, and componentCount are each set once without thread semantics,
    // but before the instance is published to other threads ... is that enough?

    private long assemblyTime;

    private int componentCount;

    private final AtomicInteger attachCount = new AtomicInteger();

    /**
     * Obtained from the {@link org.apache.tapestry5.internal.services.PersistentFieldManager} when
     * first needed,
     * discarded at the end of the request.
     */
    private final PerThreadValue<PersistentFieldBundle> fieldBundle;

    private static final Pattern SPLIT_ON_DOT = Pattern.compile("\\.");

    /**
     * @param name                   canonicalized page name
     * @param selector               used to locate resources
     * @param persistentFieldManager for access to cross-request persistent values
     * @param perThreadManager       for managing per-request mutable state
     */
    public PageImpl(String name, ComponentResourceSelector selector, PersistentFieldManager persistentFieldManager,
                    PerthreadManager perThreadManager)
    {
        this.name = name;
        this.selector = selector;
        this.persistentFieldManager = persistentFieldManager;

        fieldBundle = perThreadManager.createValue();
    }

    public void setStats(long assemblyTime, int componentCount)
    {
        this.assemblyTime = assemblyTime;
        this.componentCount = componentCount;
    }

    @Override
    public String toString()
    {
        return String.format("Page[%s %s]", name, selector.toShortString());
    }

    public synchronized ComponentPageElement getComponentElementByNestedId(String nestedId)
    {
        assert nestedId != null;

        if (nestedId.equals(""))
            return rootElement;

        ComponentPageElement element = idToComponent.get(nestedId);

        if (element == null)
        {
            element = rootElement;

            for (String id : SPLIT_ON_DOT.split(nestedId))
            {
                element = element.getEmbeddedElement(id);
            }

            idToComponent.put(nestedId, element);
        }

        return element;
    }

    public ComponentResourceSelector getSelector()
    {
        return selector;
    }

    public void setRootElement(ComponentPageElement component)
    {
        lock.check();

        rootElement = component;
    }

    public ComponentPageElement getRootElement()
    {
        return rootElement;
    }

    public Component getRootComponent()
    {
        return rootElement.getComponent();
    }

    public void addLifecycleListener(PageLifecycleListener listener)
    {
        lock.check();

        lifecycleListeners.add(listener);
    }

    public void removeLifecycleListener(PageLifecycleListener listener)
    {
        lock.check();

        lifecycleListeners.remove(listener);
    }

    public boolean detached()
    {
        boolean result = false;

        for (PageLifecycleListener listener : lifecycleListeners)
        {
            try
            {
                listener.containingPageDidDetach();
            } catch (RuntimeException ex)
            {
                getLogger().error(StructureMessages.detachFailure(listener, ex), ex);
                result = true;
            }
        }

        return result;
    }

    public void loaded()
    {
        lock.check();

        for (PageLifecycleListener listener : lifecycleListeners)
            listener.containingPageDidLoad();

        loadComplete = true;

        lock.lock();
    }

    public void attached()
    {
        attachCount.incrementAndGet();

        for (PageLifecycleListener listener : lifecycleListeners)
            listener.restoreStateBeforePageAttach();

        for (PageLifecycleListener listener : lifecycleListeners)
            listener.containingPageDidAttach();
    }

    public Logger getLogger()
    {
        return rootElement.getLogger();
    }

    public void persistFieldChange(ComponentResources resources, String fieldName, Object newValue)
    {
        if (!loadComplete)
            throw new RuntimeException(StructureMessages.persistChangeBeforeLoadComplete());

        persistentFieldManager.postChange(name, resources, fieldName, newValue);
    }

    public Object getFieldChange(String nestedId, String fieldName)
    {
        if (!fieldBundle.exists())
            fieldBundle.set(persistentFieldManager.gatherChanges(name));

        return fieldBundle.get().getValue(nestedId, fieldName);
    }

    public void discardPersistentFieldChanges()
    {
        persistentFieldManager.discardChanges(name);
    }

    public String getName()
    {
        return name;
    }

    public void addResetListener(PageResetListener listener)
    {
        assert listener != null;
        lock.check();

        resetListeners.add(listener);
    }

    public void pageReset()
    {
        for (PageResetListener l : resetListeners)
        {
            l.containingPageDidReset();
        }
    }

    public boolean hasResetListeners()
    {
        return !resetListeners.isEmpty();
    }

    public long getAssemblyTime()
    {
        return assemblyTime;
    }

    public int getComponentCount()
    {
        return componentCount;
    }

    public int getAttachCount()
    {
        return attachCount.get();
    }
}
