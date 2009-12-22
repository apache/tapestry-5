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

package org.apache.tapestry5.internal.structure;

import org.apache.tapestry5.ComponentResources;
import org.apache.tapestry5.Link;
import org.apache.tapestry5.internal.services.LinkFactory;
import org.apache.tapestry5.internal.services.PersistentFieldManager;
import org.apache.tapestry5.ioc.internal.util.CollectionFactory;
import org.apache.tapestry5.ioc.internal.util.Defense;
import static org.apache.tapestry5.ioc.internal.util.Defense.notNull;
import org.apache.tapestry5.ioc.internal.util.InternalUtils;
import org.apache.tapestry5.runtime.Component;
import org.apache.tapestry5.runtime.PageLifecycleListener;
import org.apache.tapestry5.services.ComponentClassResolver;
import org.apache.tapestry5.services.PersistentFieldBundle;
import org.slf4j.Logger;

import java.util.List;
import java.util.Locale;

public class PageImpl implements Page
{
    private final String logicalPageName;

    private final Locale locale;

    private final LinkFactory linkFactory;

    private final PersistentFieldManager persistentFieldManager;

    private final ComponentClassResolver componentClassResolver;

    private ComponentPageElement rootElement;

    private final List<PageLifecycleListener> listeners = CollectionFactory.newList();

    private int dirtyCount;

    private boolean loadComplete;

    /**
     * Obtained from the {@link org.apache.tapestry5.internal.services.PersistentFieldManager} when first needed,
     * discarded at the end of the request.
     */
    private PersistentFieldBundle fieldBundle;

    public PageImpl(String logicalPageName, Locale locale, LinkFactory linkFactory,
                    PersistentFieldManager persistentFieldManager, ComponentClassResolver componentClassResolver)
    {
        this.logicalPageName = logicalPageName;
        this.locale = locale;
        this.linkFactory = linkFactory;
        this.persistentFieldManager = persistentFieldManager;
        this.componentClassResolver = componentClassResolver;
    }

    @Override
    public String toString()
    {
        return String.format("Page[%s %s]", logicalPageName, locale);
    }

    public ComponentPageElement getComponentElementByNestedId(String nestedId)
    {
        notNull(nestedId, "nestedId");

        // TODO: Especially with the addition of all the caseless logic, and with respect to how
        // forms are implemented, it may be worthwhile to cache the key to element mapping. I think
        // we're going to do it a lot!

        ComponentPageElement element = rootElement;

        if (InternalUtils.isNonBlank(nestedId))
        {
            for (String id : nestedId.split("\\."))
                element = element.getEmbeddedElement(id);
        }

        return element;
    }

    public Locale getLocale()
    {
        return locale;
    }

    public void setRootElement(ComponentPageElement component)
    {
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
        listeners.add(listener);
    }

    public boolean detached()
    {
        boolean result = dirtyCount > 0;

        for (PageLifecycleListener listener : listeners)
        {
            try
            {
                listener.containingPageDidDetach();
            }
            catch (RuntimeException ex)
            {
                getLogger().error(StructureMessages.detachFailure(listener, ex), ex);
                result = true;
            }
        }

        fieldBundle = null;

        return result;
    }

    public void loaded()
    {
        for (PageLifecycleListener listener : listeners)
            listener.containingPageDidLoad();

        loadComplete = true;
    }

    public boolean isLoadComplete()
    {
        return loadComplete;
    }

    public void attached()
    {
        if (dirtyCount != 0) throw new IllegalStateException(StructureMessages.pageIsDirty(this));

        for (PageLifecycleListener listener : listeners)
            listener.containingPageDidAttach();
    }

    public Logger getLogger()
    {
        return rootElement.getLogger();
    }

    public Link createComponentEventLink(String nestedId, String eventType, boolean forForm, Object... context)
    {
        return linkFactory.createComponentEventLink(this, nestedId, eventType, forForm, context);
    }

    public Link createPageRenderLink(String pageName, boolean override, Object... context)
    {
        return linkFactory.createPageRenderLink(pageName, override, context);
    }

    public Link createPageRenderLink(Class pageClass, boolean override, Object... context)
    {
        Defense.notNull(pageClass, "pageClass");

        String pageName = componentClassResolver.resolvePageClassNameToPageName(pageClass.getName());

        return linkFactory.createPageRenderLink(pageName, override, context);
    }

    public void persistFieldChange(ComponentResources resources, String fieldName, Object newValue)
    {
        if (!loadComplete)
            throw new RuntimeException(StructureMessages.persistChangeBeforeLoadComplete());

        persistentFieldManager.postChange(logicalPageName, resources, fieldName, newValue);
    }

    public Object getFieldChange(String nestedId, String fieldName)
    {
        if (fieldBundle == null) fieldBundle = persistentFieldManager.gatherChanges(logicalPageName);

        return fieldBundle.getValue(nestedId, fieldName);
    }

    public void decrementDirtyCount()
    {
        dirtyCount--;
    }

    public void discardPersistentFieldChanges()
    {
        persistentFieldManager.discardChanges(logicalPageName);
    }

    public void incrementDirtyCount()
    {
        dirtyCount++;
    }

    public String getLogicalName()
    {
        return logicalPageName;
    }
}
