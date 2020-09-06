// Copyright 2010, 2011, 2012 The Apache Software Foundation
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

import org.apache.tapestry5.commons.*;
import org.apache.tapestry5.commons.internal.util.LockSupport;
import org.apache.tapestry5.commons.services.InvalidationListener;
import org.apache.tapestry5.commons.services.PlasticProxyFactory;
import org.apache.tapestry5.commons.util.CollectionFactory;
import org.apache.tapestry5.ioc.services.ThreadLocale;
import org.apache.tapestry5.services.messages.ComponentMessagesSource;

import java.util.Locale;
import java.util.Map;

/**
 * Allows for injection of the global application message catalog into services. The injected value
 * is, in fact, a proxy. Each method access of the proxy will determine the current thread's locale, and delegate
 * to the actual global message catalog for that particular locale. There's caching to keep it reasonably
 * efficient.
 *
 * @see ComponentMessagesSource#getApplicationCatalog(Locale)
 * @since 5.2.0
 */
public class ApplicationMessageCatalogObjectProvider extends LockSupport implements ObjectProvider
{
    private final ObjectLocator objectLocator;

    private ComponentMessagesSource messagesSource;

    private ThreadLocale threadLocale;

    private final Map<Locale, Messages> localeToMessages = CollectionFactory.newConcurrentMap();

    private Messages proxy;

    private class ApplicationMessagesObjectCreator implements ObjectCreator<Messages>
    {
        public Messages createObject()
        {
            Locale locale = threadLocale.getLocale();

            Messages messages = localeToMessages.get(locale);

            if (messages == null)
            {
                messages = messagesSource.getApplicationCatalog(locale);
                localeToMessages.put(locale, messages);
            }

            return messages;
        }
    }

    ;

    public ApplicationMessageCatalogObjectProvider(ObjectLocator locator)
    {
        this.objectLocator = locator;
    }

    /**
     * Because this is an ObjectProvider and is part of the MasterObjectProvider pipeline, it has to
     * be careful to not require further dependencies at construction time. This means we have to "drop out"
     * of normal IoC dependency injection and adopt a lookup strategy based on the ObjectLocator. Further,
     * we have to be careful about multi-threading issues.
     */
    private Messages getProxy()
    {
        try
        {
            acquireReadLock();

            if (proxy == null)
            {
                createProxy();
            }

            return proxy;
        } finally
        {
            releaseReadLock();
        }
    }

    private void createProxy()
    {
        try
        {
            upgradeReadLockToWriteLock();

            this.messagesSource = objectLocator.getService(ComponentMessagesSource.class);
            this.threadLocale = objectLocator.getService(ThreadLocale.class);

            PlasticProxyFactory proxyFactory = objectLocator.getService("PlasticProxyFactory",
                    PlasticProxyFactory.class);

            proxy = proxyFactory.createProxy(Messages.class, new ApplicationMessagesObjectCreator(),
                    "<ApplicationMessagesProxy>");

            // Listen for invalidations; clear our cache of localized Messages bundles when
            // an invalidation occurs.

            messagesSource.getInvalidationEventHub().clearOnInvalidation(localeToMessages);
        } finally
        {
            downgradeWriteLockToReadLock();
        }
    }

    public <T> T provide(Class<T> objectType, AnnotationProvider annotationProvider, ObjectLocator locator)
    {
        if (objectType.equals(Messages.class))
            return objectType.cast(getProxy());

        return null;
    }

    public void objectWasInvalidated()
    {
        localeToMessages.clear();
    }

}
