// Copyright 2006-2014 The Apache Software Foundation
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
import org.apache.tapestry5.MetaDataConstants;
import org.apache.tapestry5.commons.ObjectCreator;
import org.apache.tapestry5.commons.util.CollectionFactory;
import org.apache.tapestry5.commons.util.ExceptionUtils;
import org.apache.tapestry5.internal.services.PersistentFieldManager;
import org.apache.tapestry5.ioc.internal.util.OneShotLock;
import org.apache.tapestry5.ioc.services.PerthreadManager;
import org.apache.tapestry5.runtime.Component;
import org.apache.tapestry5.runtime.PageLifecycleListener;
import org.apache.tapestry5.services.MetaDataLocator;
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

    private List<Runnable> loadedCallbacks = CollectionFactory.newList();

    private final List<Runnable> attachCallbacks = CollectionFactory.newList();

    private final List<Runnable> detachCallbacks = CollectionFactory.newList();

    private final List<Runnable> resetCallbacks = CollectionFactory.newList();

    private boolean loadComplete;

    private final OneShotLock lifecycleListenersLock = new OneShotLock();

    private final OneShotLock verifyListenerLocks = new OneShotLock();

    // May end up with multiple mappings for the same id (with different case) to the same component.
    // That's OK.
    private final Map<String, ComponentPageElement> idToComponent = CollectionFactory.newConcurrentMap();

    private Stats stats;

    private final AtomicInteger attachCount = new AtomicInteger();

    private final boolean exactParameterCountMatch;

    private List<Runnable> pageVerifyCallbacks = CollectionFactory.newList();

    /**
     * Obtained from the {@link org.apache.tapestry5.internal.services.PersistentFieldManager} when
     * first needed,
     * discarded at the end of the request.
     */
    private final ObjectCreator<PersistentFieldBundle> fieldBundle;

    private static final Pattern SPLIT_ON_DOT = Pattern.compile("\\.");

    /**
     * @param name
     *         canonicalized page name
     * @param selector
     *         used to locate resources
     * @param persistentFieldManager
     *         for access to cross-request persistent values
     * @param perThreadManager
     * @param metaDataLocator
     */
    public PageImpl(String name, ComponentResourceSelector selector, PersistentFieldManager persistentFieldManager,
                    PerthreadManager perThreadManager, MetaDataLocator metaDataLocator)
    {
        this.name = name;
        this.selector = selector;
        this.persistentFieldManager = persistentFieldManager;

        fieldBundle = perThreadManager.createValue(new ObjectCreator<PersistentFieldBundle>() {
            @Override
            public PersistentFieldBundle createObject() {
                return PageImpl.this.persistentFieldManager.gatherChanges(PageImpl.this.name);
            }
        });


        exactParameterCountMatch = metaDataLocator.findMeta(MetaDataConstants.UNKNOWN_ACTIVATION_CONTEXT_CHECK, name, Boolean.class);
    }

    public void setStats(Stats stats)
    {
        this.stats = stats;
    }

    public Stats getStats()
    {
        return stats;
    }

    @Override
    public String toString()
    {
        return String.format("Page[%s %s]", name, selector.toShortString());
    }

    public ComponentPageElement getComponentElementByNestedId(String nestedId)
    {
        assert nestedId != null;

        if (nestedId.equals(""))
        {
            return rootElement;
        }

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
        lifecycleListenersLock.check();

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

    public void addLifecycleListener(final PageLifecycleListener listener)
    {
        assert listener != null;

        addPageLoadedCallback(new Runnable()
        {
            public void run()
            {
                listener.containingPageDidLoad();
            }
        });

        addPageAttachedCallback(new Runnable()
        {
            public void run()
            {
                listener.containingPageDidAttach();
            }
        });

        addPageDetachedCallback(new Runnable()
        {
            public void run()
            {
                listener.containingPageDidDetach();
            }
        });
    }

    public void removeLifecycleListener(PageLifecycleListener listener)
    {
        lifecycleListenersLock.check();

        throw new UnsupportedOperationException("It is not longer possible to remove a page lifecycle listener; please convert your code to use the addPageLoadedCallback() method instead.");
    }

    public boolean detached()
    {
        boolean result = false;

        for (Runnable callback : detachCallbacks)
        {
            try
            {
                callback.run();
            } catch (RuntimeException ex)
            {
                result = true;

                getLogger().error(String.format("Callback %s failed during page detach: %s", callback, ExceptionUtils.toMessage(ex)), ex);
            }
        }

        return result;
    }

    public void loaded()
    {
        lifecycleListenersLock.lock();

        invokeCallbacks(loadedCallbacks);

        loadedCallbacks = null;

        verifyListenerLocks.lock();

        invokeCallbacks(pageVerifyCallbacks);

        pageVerifyCallbacks = null;

        loadComplete = true;
    }

    public void attached()
    {
        attachCount.incrementAndGet();

        invokeCallbacks(attachCallbacks);
    }

    public Logger getLogger()
    {
        return rootElement.getLogger();
    }

    public void persistFieldChange(ComponentResources resources, String fieldName, Object newValue)
    {
        if (!loadComplete)
        {
            throw new RuntimeException(StructureMessages.persistChangeBeforeLoadComplete());
        }

        persistentFieldManager.postChange(name, resources, fieldName, newValue);
    }

    public Object getFieldChange(String nestedId, String fieldName)
    {
        return fieldBundle.createObject().getValue(nestedId, fieldName);
    }

    public void discardPersistentFieldChanges()
    {
        persistentFieldManager.discardChanges(name);
    }

    public String getName()
    {
        return name;
    }

    public void addResetCallback(Runnable callback)
    {
        assert callback != null;

        lifecycleListenersLock.check();

        resetCallbacks.add(callback);
    }

    public void addResetListener(final PageResetListener listener)
    {
        assert listener != null;

        addResetCallback(new Runnable()
        {
            public void run()
            {
                listener.containingPageDidReset();
            }
        });
    }

    public void addVerifyCallback(Runnable callback)
    {
        verifyListenerLocks.check();

        assert callback != null;

        pageVerifyCallbacks.add(callback);
    }

    public void pageReset()
    {
        invokeCallbacks(resetCallbacks);
    }

    public boolean hasResetListeners()
    {
        return !resetCallbacks.isEmpty();
    }

    public int getAttachCount()
    {
        return attachCount.get();
    }

    public boolean isExactParameterCountMatch()
    {
        return exactParameterCountMatch;
    }

    public void addPageLoadedCallback(Runnable callback)
    {
        lifecycleListenersLock.check();

        assert callback != null;

        loadedCallbacks.add(callback);
    }

    public void addPageAttachedCallback(Runnable callback)
    {
        lifecycleListenersLock.check();

        assert callback != null;

        attachCallbacks.add(callback);
    }

    public void addPageDetachedCallback(Runnable callback)
    {
        lifecycleListenersLock.check();

        assert callback != null;

        detachCallbacks.add(callback);
    }

    private void invokeCallbacks(List<Runnable> callbacks)
    {
        for (Runnable callback : callbacks)
        {
            callback.run();
        }
    }
}
