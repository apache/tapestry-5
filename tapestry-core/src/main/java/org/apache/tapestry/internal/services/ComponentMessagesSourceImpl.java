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

    private static class ComponentModelBundle implements MessagesBundle
    {
        private final ComponentModel _model;

        public ComponentModelBundle(final ComponentModel model)
        {
            _model = model;
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

            return parentModel != null ? new ComponentModelBundle(parentModel) : null;
        }

    }

    public ComponentMessagesSourceImpl()
    {
        this(new URLChangeTracker());
    }

    ComponentMessagesSourceImpl(URLChangeTracker tracker)
    {
        _messagesSource = new MessagesSourceImpl(tracker);
    }

    public void checkForUpdates()
    {
        _messagesSource.checkForUpdates();
    }

    public Messages getMessages(ComponentModel componentModel, Locale locale)
    {
        MessagesBundle bundle = new ComponentModelBundle(componentModel);

        return _messagesSource.getMessages(bundle, locale);
    }

    public void addInvalidationListener(InvalidationListener listener)
    {
        _messagesSource.addInvalidationListener(listener);
    }
}
