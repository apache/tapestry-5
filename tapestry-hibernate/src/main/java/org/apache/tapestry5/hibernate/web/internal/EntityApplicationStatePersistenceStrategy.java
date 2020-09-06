// Copyright 2009, 2014 The Apache Software Foundation
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

package org.apache.tapestry5.hibernate.web.internal;

import org.apache.tapestry5.http.services.Request;
import org.apache.tapestry5.internal.services.SessionApplicationStatePersistenceStrategy;
import org.apache.tapestry5.services.ApplicationStateCreator;
import org.hibernate.Session;

/**
 * Persists Hibernate entities as SSOs by storing their primary key in the {@link org.apache.tapestry5.http.services.Session}.
 *
 * @see org.apache.tapestry5.hibernate.web.internal.PersistedEntity
 */
public class EntityApplicationStatePersistenceStrategy extends SessionApplicationStatePersistenceStrategy
{

    private final EntityPersistentFieldStrategy delegate;

    public EntityApplicationStatePersistenceStrategy(Request request, Session hibernateSession)
    {
        super(request);

        delegate = new EntityPersistentFieldStrategy(hibernateSession, null);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T get(Class<T> ssoClass, ApplicationStateCreator<T> creator)
    {
        final Object persistedValue = getOrCreate(ssoClass, creator);

        if (persistedValue instanceof SessionRestorable)
        {
            Object restored = delegate.convertPersistedToApplicationValue(persistedValue);

            // Maybe throw an exception instead?
            if (restored == null)
            {
                set(ssoClass, null);
                return (T) getOrCreate(ssoClass, creator);
            }

            return (T) restored;
        }

        return (T) persistedValue;
    }

    @Override
    public <T> void set(Class<T> ssoClass, T sso)
    {
        final String key = buildKey(ssoClass);

        if (sso == null)
        {
            getSession().setAttribute(key, null);
            return;
        }

        Object persistable = delegate.convertApplicationValueToPersisted(sso);

        getSession().setAttribute(key, persistable);
    }

}