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

import static org.apache.tapestry.ioc.internal.util.CollectionFactory.newConcurrentMap;

import java.util.Collection;
import java.util.Locale;
import java.util.Map;

import org.apache.tapestry.events.UpdateListener;
import org.apache.tapestry.internal.util.URLChangeTracker;
import org.apache.tapestry.ioc.MessageFormatter;
import org.apache.tapestry.ioc.Messages;
import org.apache.tapestry.ioc.Resource;
import org.apache.tapestry.services.ValidationMessagesSource;

public class ValidationMessagesSourceImpl implements ValidationMessagesSource, UpdateListener
{
    private final MessagesSource _messagesSource;

    private final MessagesBundle _bundle;

    private final Map<Locale, Messages> _cache = newConcurrentMap();

    private class ValidationMessagesBundle implements MessagesBundle
    {
        private final Resource _baseResource;

        private final MessagesBundle _parent;

        public ValidationMessagesBundle(final Resource baseResource, final MessagesBundle parent)
        {
            _baseResource = baseResource;
            _parent = parent;
        }

        public Resource getBaseResource()
        {
            return _baseResource;
        }

        public Object getId()
        {
            return _baseResource.getPath();
        }

        public MessagesBundle getParent()
        {
            return _parent;
        }

    };

    /**
     * Delegates to a {@link Messages} instance obtained from the {@link MessagesSource}. This
     * ensures that changes to the underlying properties files will be reflected.
     */
    private class ValidationMessages implements Messages
    {
        private final Locale _locale;

        public ValidationMessages(final Locale locale)
        {
            _locale = locale;
        }

        private Messages messages()
        {
            // The MessagesSource caches the value returned, until any underlying file is touched,
            // at which point an updated Messages will be returned.

            return _messagesSource.getMessages(_bundle, _locale);
        }

        public boolean contains(String key)
        {
            return messages().contains(key);
        }

        public String format(String key, Object... args)
        {
            return messages().format(key, args);
        }

        public String get(String key)
        {
            return messages().get(key);
        }

        public MessageFormatter getFormatter(String key)
        {
            // Ideally, there would be a MessageFormatterImpl that would delegate to a fresh copy
            // of a MessageFormatter obtained from the source, but that's probably overkill.

            return messages().getFormatter(key);
        }
    }

    public ValidationMessagesSourceImpl(Collection<String> bundles, Resource classpathRoot)
    {
        this(bundles, classpathRoot, new URLChangeTracker());
    }

    ValidationMessagesSourceImpl(Collection<String> bundles, Resource classpathRoot,
            URLChangeTracker tracker)
    {
        _messagesSource = new MessagesSourceImpl(tracker);

        MessagesBundle parent = null;

        for (String bundle : bundles)
        {
            Resource bundleResource = classpathRoot.forFile(bundle);

            parent = new ValidationMessagesBundle(bundleResource, parent);
        }

        _bundle = parent;
    }

    public Messages getValidationMessages(Locale locale)
    {
        Messages result = _cache.get(locale);

        if (result == null)
        {
            result = new ValidationMessages(locale);
            _cache.put(locale, result);
        }

        return result;
    }

    public void checkForUpdates()
    {
        // When there are changes, the Messages cached inside the MessagesSource will be discarded
        // and will be rebuilt on demand by the ValidatonMessages instances.

        _messagesSource.checkForUpdates();
    }

}
