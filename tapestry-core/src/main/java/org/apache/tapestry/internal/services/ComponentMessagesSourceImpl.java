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

import java.util.Locale;

import org.apache.tapestry.events.InvalidationListener;
import org.apache.tapestry.events.UpdateListener;
import org.apache.tapestry.internal.util.URLChangeTracker;
import org.apache.tapestry.ioc.Messages;
import org.apache.tapestry.ioc.Resource;
import org.apache.tapestry.model.ComponentModel;
import org.apache.tapestry.services.ComponentMessagesSource;

public class ComponentMessagesSourceImpl implements ComponentMessagesSource, UpdateListener
{
    private final MessagesSource _messagesSource;

    private final Resource _rootResource;

    private final String _appCatalog;

    private static class ComponentModelBundle implements MessagesBundle
    {
        private final ComponentModel _model;

        private final MessagesBundle _rootBundle;

        public ComponentModelBundle(ComponentModel model, MessagesBundle rootBundle)
        {
            _model = model;
            _rootBundle = rootBundle;
        }

        public Resource getBaseResource()
        {
            return _model.getBaseResource();
        }

        public Object getId()
        {
            return _model.getComponentClassName();
        }

        public MessagesBundle getParent()
        {
            ComponentModel parentModel = _model.getParentModel();

            if (parentModel == null) return _rootBundle;

            return new ComponentModelBundle(parentModel, _rootBundle);
        }
    }

    public ComponentMessagesSourceImpl(Resource rootResource, String appCatalog)
    {
        this(rootResource, appCatalog, new URLChangeTracker());
    }

    ComponentMessagesSourceImpl(Resource rootResource, String appCatalog, URLChangeTracker tracker)
    {
        _rootResource = rootResource;
        _appCatalog = appCatalog;

        _messagesSource = new MessagesSourceImpl(tracker);
    }

    public void checkForUpdates()
    {
        _messagesSource.checkForUpdates();
    }

    public Messages getMessages(ComponentModel componentModel, Locale locale)
    {
        final Resource appCatalogResource = _rootResource.forFile(_appCatalog);

        // If the application catalog exists, set it up as the root, otherwise use null.

        MessagesBundle appCatalogBundle = appCatalogResource.toURL() == null ? null
                : new MessagesBundle()
                {
                    public Resource getBaseResource()
                    {
                        return appCatalogResource;
                    }

                    public Object getId()
                    {
                        return _appCatalog;
                    }

                    public MessagesBundle getParent()
                    {
                        return null;
                    }
                };

        MessagesBundle bundle = new ComponentModelBundle(componentModel, appCatalogBundle);

        return _messagesSource.getMessages(bundle, locale);
    }

    public void addInvalidationListener(InvalidationListener listener)
    {
        _messagesSource.addInvalidationListener(listener);
    }
}
