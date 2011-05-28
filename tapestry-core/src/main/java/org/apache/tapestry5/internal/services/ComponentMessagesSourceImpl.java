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

package org.apache.tapestry5.internal.services;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.apache.tapestry5.SymbolConstants;
import org.apache.tapestry5.func.Worker;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.Resource;
import org.apache.tapestry5.ioc.annotations.Symbol;
import org.apache.tapestry5.ioc.internal.util.CollectionFactory;
import org.apache.tapestry5.ioc.internal.util.URLChangeTracker;
import org.apache.tapestry5.ioc.services.ClasspathURLConverter;
import org.apache.tapestry5.model.ComponentModel;
import org.apache.tapestry5.services.InvalidationEventHub;
import org.apache.tapestry5.services.UpdateListener;
import org.apache.tapestry5.services.messages.ComponentMessagesSource;
import org.apache.tapestry5.services.messages.PropertiesFileParser;
import org.apache.tapestry5.services.pageload.ComponentResourceLocator;
import org.apache.tapestry5.services.pageload.ComponentResourceSelector;

public class ComponentMessagesSourceImpl implements ComponentMessagesSource, UpdateListener
{
    private final MessagesSource messagesSource;

    private final MessagesBundle appCatalogBundle;

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

    public ComponentMessagesSourceImpl(@Symbol(SymbolConstants.PRODUCTION_MODE)
    boolean productionMode, List<Resource> appCatalogResources, PropertiesFileParser parser,
            ComponentResourceLocator resourceLocator, ClasspathURLConverter classpathURLConverter)
    {
        this(productionMode, appCatalogResources, resourceLocator, parser, new URLChangeTracker(classpathURLConverter));
    }

    ComponentMessagesSourceImpl(boolean productionMode, Resource appCatalogResource,
            ComponentResourceLocator resourceLocator, PropertiesFileParser parser, URLChangeTracker tracker)
    {
        this(productionMode, Arrays.asList(appCatalogResource), resourceLocator, parser, tracker);
    }

    ComponentMessagesSourceImpl(boolean productionMode, List<Resource> appCatalogResources,
            ComponentResourceLocator resourceLocator, PropertiesFileParser parser, URLChangeTracker tracker)
    {
        messagesSource = new MessagesSourceImpl(productionMode, productionMode ? null : tracker, resourceLocator,
                parser);

        appCatalogBundle = createAppCatalogBundle(appCatalogResources);
    }

    public void checkForUpdates()
    {
        messagesSource.checkForUpdates();
    }

    public Messages getMessages(ComponentModel componentModel, Locale locale)
    {
        return getMessages(componentModel, new ComponentResourceSelector(locale));
    }

    public Messages getMessages(ComponentModel componentModel, ComponentResourceSelector selector)
    {
        MessagesBundle bundle = new ComponentModelBundle(componentModel);

        return messagesSource.getMessages(bundle, selector);
    }

    public Messages getApplicationCatalog(Locale locale)
    {
        return messagesSource.getMessages(appCatalogBundle, new ComponentResourceSelector(locale));
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
