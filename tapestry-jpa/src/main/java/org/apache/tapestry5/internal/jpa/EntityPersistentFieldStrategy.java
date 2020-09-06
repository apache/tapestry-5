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
import org.apache.tapestry5.internal.services.AbstractSessionPersistentFieldStrategy;
import org.apache.tapestry5.jpa.EntityManagerManager;

/**
 * Persists JPA entities by storing their id in the session.
 */
public class EntityPersistentFieldStrategy extends AbstractSessionPersistentFieldStrategy
{
    private final EntityManagerManager entityManagerManager;

    public EntityPersistentFieldStrategy(final EntityManagerManager entityManagerManager,
            final Request request)
    {
        super("entity:", request);

        this.entityManagerManager = entityManagerManager;
    }

    @Override
    public Object convertApplicationValueToPersisted(final Object newValue)
    {
        return JpaInternalUtils.convertApplicationValueToPersisted(entityManagerManager, newValue);
    }

    @Override
    public Object convertPersistedToApplicationValue(final Object persistedValue)
    {
        final PersistedEntity persisted = (PersistedEntity) persistedValue;

        return persisted.restore(entityManagerManager);
    }
}
