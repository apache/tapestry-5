// Copyright 2006-2013 The Apache Software Foundation
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

package org.apache.tapestry5.internal.services;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Callable;

import org.apache.tapestry5.commons.Messages;
import org.apache.tapestry5.commons.Resource;
import org.apache.tapestry5.commons.internal.util.TapestryException;
import org.apache.tapestry5.commons.services.InvalidationEventHub;
import org.apache.tapestry5.http.TapestryHttpSymbolConstants;
import org.apache.tapestry5.ioc.annotations.PostInjection;
import org.apache.tapestry5.ioc.annotations.Symbol;
import org.apache.tapestry5.ioc.internal.util.URLChangeTracker;
import org.apache.tapestry5.ioc.services.ClasspathURLConverter;
import org.apache.tapestry5.ioc.services.ThreadLocale;
import org.apache.tapestry5.ioc.services.UpdateListener;
import org.apache.tapestry5.model.ComponentModel;
import org.apache.tapestry5.services.messages.ComponentMessagesSource;
import org.apache.tapestry5.services.messages.PropertiesFileParser;
import org.apache.tapestry5.services.pageload.ComponentRequestSelectorAnalyzer;
import org.apache.tapestry5.services.pageload.ComponentResourceLocator;
import org.apache.tapestry5.services.pageload.ComponentResourceSelector;

public class ComponentMessagesSourceImpl implements ComponentMessagesSource, UpdateListener
{
    private final MessagesSourceImpl messagesSource;

    private final MessagesBundle appCatalogBundle;
    
    private final ComponentRequestSelectorAnalyzer componentRequestSelectorAnalyzer;
    
    private final ThreadLocale threadLocale;

    private class ComponentModelBundle implements MessagesBundle
    {
        private final ComponentModel model;

        public ComponentModelBundle(ComponentModel model)
        {
            this.model = model;
        }

        public Resource getBaseResource()
        {
            return model.getBaseResource();
        }

        public Object getId()
        {
            return model.getComponentClassName();
        }

        public MessagesBundle getParent()
        {
            ComponentModel parentModel = model.getParentModel();

            if (parentModel == null)
                return appCatalogBundle;

            return new ComponentModelBundle(parentModel);
        }
    }

    public ComponentMessagesSourceImpl(@Symbol(TapestryHttpSymbolConstants.PRODUCTION_MODE)
                                       boolean productionMode, List<Resource> appCatalogResources, PropertiesFileParser parser,
                                       ComponentResourceLocator resourceLocator, ClasspathURLConverter classpathURLConverter,
                                       ComponentRequestSelectorAnalyzer componentRequestSelectorAnalyzer,
                                       ThreadLocale threadLocale)
    {
        this(productionMode, appCatalogResources, resourceLocator, parser, new URLChangeTracker(classpathURLConverter), componentRequestSelectorAnalyzer, threadLocale);
    }

    ComponentMessagesSourceImpl(boolean productionMode, Resource appCatalogResource,
                                ComponentResourceLocator resourceLocator, PropertiesFileParser parser, 
                                URLChangeTracker tracker, ComponentRequestSelectorAnalyzer componentRequestSelectorAnalyzer,
                                ThreadLocale threadLocale)
    {
        this(productionMode, Arrays.asList(appCatalogResource), resourceLocator, parser, tracker, componentRequestSelectorAnalyzer, threadLocale);
    }

    ComponentMessagesSourceImpl(boolean productionMode, List<Resource> appCatalogResources,
                                ComponentResourceLocator resourceLocator, PropertiesFileParser parser, 
                                URLChangeTracker tracker, ComponentRequestSelectorAnalyzer componentRequestSelectorAnalyzer,
                                ThreadLocale threadLocale)
    {
        messagesSource = new MessagesSourceImpl(productionMode, productionMode ? null : tracker, resourceLocator,
                parser);

        appCatalogBundle = createAppCatalogBundle(appCatalogResources);
        this.componentRequestSelectorAnalyzer = componentRequestSelectorAnalyzer;
        this.threadLocale = threadLocale;
    }

    @PostInjection
    public void setupReload(ReloadHelper reloadHelper)
    {
        reloadHelper.addReloadCallback(new Runnable()
        {
            public void run()
            {
                messagesSource.invalidate();
            }
        });
    }

    public void checkForUpdates()
    {
        messagesSource.checkForUpdates();
    }

    public Messages getMessages(ComponentModel componentModel, Locale locale)
    {
       return getMessagesWithForcedLocale(() -> getMessages(componentModel, componentRequestSelectorAnalyzer.buildSelectorForRequest()), locale);
    }
    
    private Messages getMessagesWithForcedLocale(Callable<Messages> callable, Locale locale)
    {
        final Locale original = threadLocale.getLocale();
        try 
        {
           threadLocale.setLocale(locale);
           return callable.call();
        } catch (Exception e) {
            throw new TapestryException(e.getMessage(), e);
        }
        finally {
            threadLocale.setLocale(original);
        }
    }

    public Messages getMessages(ComponentModel componentModel, ComponentResourceSelector selector)
    {
        final MessagesBundle bundle = new ComponentModelBundle(componentModel);
        return messagesSource.getMessages(bundle, selector);
    }
    
    public Messages getApplicationCatalog(Locale locale)
    {
        return getMessagesWithForcedLocale(() -> messagesSource.getMessages(appCatalogBundle, componentRequestSelectorAnalyzer.buildSelectorForRequest()), locale);
    }

    private MessagesBundle createAppCatalogBundle(List<Resource> resources)
    {
        MessagesBundle current = null;

        for (Resource r : resources)
        {
            current = createMessagesBundle(r, current);
        }

        return current;
    }

    private MessagesBundle createMessagesBundle(final Resource resource, final MessagesBundle parent)
    {
        return new MessagesBundle()
        {
            public Resource getBaseResource()
            {
                return resource;
            }

            public Object getId()
            {
                return resource.getPath();
            }

            public MessagesBundle getParent()
            {
                return parent;
            }
        };
    }

    public InvalidationEventHub getInvalidationEventHub()
    {
        return messagesSource;
    }
}
