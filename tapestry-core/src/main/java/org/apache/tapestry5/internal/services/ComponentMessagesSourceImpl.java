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

package org.apache.tapestry5.internal.services;

import org.apache.tapestry5.internal.events.InvalidationListener;
import org.apache.tapestry5.internal.events.UpdateListener;
import org.apache.tapestry5.internal.util.URLChangeTracker;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.Resource;
import org.apache.tapestry5.model.ComponentModel;

import java.util.Locale;

public class ComponentMessagesSourceImpl implements ComponentMessagesSource, UpdateListener
{
    private final MessagesSource messagesSource;

    private final Resource rootResource;

    private final String appCatalog;

    private static class ComponentModelBundle implements MessagesBundle
    {
        private final ComponentModel model;

        private final MessagesBundle rootBundle;

        public ComponentModelBundle(ComponentModel model, MessagesBundle rootBundle)
        {
            this.model = model;
            this.rootBundle = rootBundle;
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

            if (parentModel == null) return rootBundle;

            return new ComponentModelBundle(parentModel, rootBundle);
        }
    }

    public ComponentMessagesSourceImpl(Resource rootResource, String appCatalog)
    {
        this(rootResource, appCatalog, new URLChangeTracker());
    }

    ComponentMessagesSourceImpl(Resource rootResource, String appCatalog, URLChangeTracker tracker)
    {
        this.rootResource = rootResource;
        this.appCatalog = appCatalog;

        messagesSource = new MessagesSourceImpl(tracker);
    }

    public void checkForUpdates()
    {
        messagesSource.checkForUpdates();
    }

    public Messages getMessages(ComponentModel componentModel, Locale locale)
    {
        final Resource appCatalogResource = rootResource.forFile(appCatalog);

        // If the application catalog exists, set it up as the root, otherwise use null.

        MessagesBundle appCatalogBundle = !appCatalogResource.exists() ? null
                                          : new MessagesBundle()
        {
            public Resource getBaseResource()
            {
                return appCatalogResource;
            }

            public Object getId()
            {
                return appCatalog;
            }

            public MessagesBundle getParent()
            {
                return null;
            }
        };

        MessagesBundle bundle = new ComponentModelBundle(componentModel, appCatalogBundle);

        return messagesSource.getMessages(bundle, locale);
    }

    public void addInvalidationListener(InvalidationListener listener)
    {
        messagesSource.addInvalidationListener(listener);
    }
}
