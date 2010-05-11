// Copyright 2006, 2007, 2008, 2009, 2010 The Apache Software Foundation
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

import java.util.Locale;

import org.apache.tapestry5.SymbolConstants;
import org.apache.tapestry5.internal.util.URLChangeTracker;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.Resource;
import org.apache.tapestry5.ioc.annotations.Symbol;
import org.apache.tapestry5.ioc.services.ClasspathURLConverter;
import org.apache.tapestry5.model.ComponentModel;
import org.apache.tapestry5.services.InvalidationEventHub;
import org.apache.tapestry5.services.UpdateListener;
import org.apache.tapestry5.services.messages.ComponentMessagesSource;
import org.apache.tapestry5.services.messages.PropertiesFileParser;

public class ComponentMessagesSourceImpl implements ComponentMessagesSource, UpdateListener
{
    private final MessagesSource messagesSource;

    private final Resource appCatalogResource;

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

    public ComponentMessagesSourceImpl(@Symbol(SymbolConstants.APPLICATION_CATALOG)
    Resource appCatalogResource,

    PropertiesFileParser parser,

    ClasspathURLConverter classpathURLConverter)
    {
        this(appCatalogResource, parser, new URLChangeTracker(classpathURLConverter));
    }

    ComponentMessagesSourceImpl(Resource appCatalogResource, PropertiesFileParser parser, URLChangeTracker tracker)
    {
        this.appCatalogResource = appCatalogResource;

        messagesSource = new MessagesSourceImpl(tracker, parser);

        appCatalogBundle = createAppCatalogBundle();
    }

    public void checkForUpdates()
    {
        messagesSource.checkForUpdates();
    }

    public Messages getMessages(ComponentModel componentModel, Locale locale)
    {
        MessagesBundle bundle = new ComponentModelBundle(componentModel);

        return messagesSource.getMessages(bundle, locale);
    }

    public Messages getApplicationCatalog(Locale locale)
    {
        return messagesSource.getMessages(appCatalogBundle, locale);
    }

    private MessagesBundle createAppCatalogBundle()
    {
        return new MessagesBundle()
        {
            public Resource getBaseResource()
            {
                return appCatalogResource;
            }

            public Object getId()
            {
                return appCatalogResource.getPath();
            }

            public MessagesBundle getParent()
            {
                return null;
            }
        };
    }

    public InvalidationEventHub getInvalidationEventHub()
    {
        return messagesSource;
    }
}
