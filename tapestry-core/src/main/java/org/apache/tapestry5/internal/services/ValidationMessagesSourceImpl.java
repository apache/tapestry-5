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

package org.apache.tapestry5.internal.services;

import org.apache.tapestry5.internal.util.URLChangeTracker;
import org.apache.tapestry5.ioc.MessageFormatter;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.Resource;
import org.apache.tapestry5.ioc.internal.util.CollectionFactory;
import org.apache.tapestry5.ioc.services.ClasspathURLConverter;
import org.apache.tapestry5.services.UpdateListener;
import org.apache.tapestry5.services.ValidationMessagesSource;

import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ValidationMessagesSourceImpl implements ValidationMessagesSource, UpdateListener
{
    private final MessagesSource messagesSource;

    private final MessagesBundle bundle;

    private final Map<Locale, Messages> cache = CollectionFactory.newConcurrentMap();

    private class ValidationMessagesBundle implements MessagesBundle
    {
        private final Resource baseResource;

        private final MessagesBundle parent;

        public ValidationMessagesBundle(Resource baseResource, MessagesBundle parent)
        {
            this.baseResource = baseResource;
            this.parent = parent;
        }

        public Resource getBaseResource()
        {
            return baseResource;
        }

        public Object getId()
        {
            return baseResource.getPath();
        }

        public MessagesBundle getParent()
        {
            return parent;
        }
    }

    /**
     * Delegates to a {@link Messages} instance obtained from the {@link MessagesSource}. This ensures that changes to
     * the underlying properties files will be reflected.
     */
    private class ValidationMessages implements Messages
    {
        private final Locale locale;

        public ValidationMessages(Locale locale)
        {
            this.locale = locale;
        }

        private Messages messages()
        {
            // The MessagesSource caches the value returned, until any underlying file is touched,
            // at which point an updated Messages will be returned.

            return messagesSource.getMessages(bundle, locale);
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

    public ValidationMessagesSourceImpl(List<String> bundles, Resource classpathRoot, ClasspathURLConverter classpathURLConverter)
    {
        this(bundles, classpathRoot, new URLChangeTracker(classpathURLConverter));
    }

    ValidationMessagesSourceImpl(List<String> bundles, Resource classpathRoot, URLChangeTracker tracker)
    {
        messagesSource = new MessagesSourceImpl(tracker);

        MessagesBundle parent = null;

        for (String bundle : bundles)
        {
            Resource bundleResource = classpathRoot.forFile(bundle);

            parent = new ValidationMessagesBundle(bundleResource, parent);
        }

        bundle = parent;
    }

    public Messages getValidationMessages(Locale locale)
    {
        Messages result = cache.get(locale);

        if (result == null)
        {
            result = new ValidationMessages(locale);
            cache.put(locale, result);
        }

        return result;
    }

    public void checkForUpdates()
    {
        // When there are changes, the Messages cached inside the MessagesSource will be discarded
        // and will be rebuilt on demand by the ValidationMessages instances.

        messagesSource.checkForUpdates();
    }
}
