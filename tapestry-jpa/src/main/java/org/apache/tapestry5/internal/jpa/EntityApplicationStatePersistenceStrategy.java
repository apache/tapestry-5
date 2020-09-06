// Copyright 2011 The Apache Software Foundation
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

package org.apache.tapestry5.internal.jpa;

import org.apache.tapestry5.http.services.Request;
import org.apache.tapestry5.internal.services.SessionApplicationStatePersistenceStrategy;
import org.apache.tapestry5.jpa.EntityManagerManager;
import org.apache.tapestry5.services.ApplicationStateCreator;

public class EntityApplicationStatePersistenceStrategy extends
        SessionApplicationStatePersistenceStrategy
{
    private final EntityManagerManager entityManagerManager;

    public EntityApplicationStatePersistenceStrategy(final Request request,
            final EntityManagerManager entityManagerManager)
    {
        super(request);

        this.entityManagerManager = entityManagerManager;
    }

    @Override
    public <T> T get(final Class<T> ssoClass, final ApplicationStateCreator<T> creator)
    {
        final Object persistedValue = getOrCreate(ssoClass, creator);

        if (persistedValue instanceof PersistedEntity)
        {
            final PersistedEntity persisted = (PersistedEntity) persistedValue;

            final Object restored = persisted.restore(entityManagerManager);

            // shall we maybe throw an exception instead?
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
    public <T> void set(final Class<T> ssoClass, final T sso)
    {
        final String key = buildKey(ssoClass);

        Object entity;

        if (sso != null)
        {
            try
            {
                entity = JpaInternalUtils.convertApplicationValueToPersisted(entityManagerManager,
                        sso);
            }
            catch (final RuntimeException ex)
            {
                // if entity not attached to an EntityManager yet, store it as usual sso
                entity = sso;
            }
        }
        else
        {
            entity = sso;
        }

        getSession().setAttribute(key, entity);
    }

}
