// Copyright 2007, 2008 The Apache Software Foundation
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

import static org.apache.tapestry5.ioc.internal.util.CollectionFactory.newList;
import static org.apache.tapestry5.ioc.internal.util.Defense.notBlank;
import org.apache.tapestry5.services.PersistentFieldChange;
import org.apache.tapestry5.services.PersistentFieldStrategy;
import org.apache.tapestry5.services.Request;
import org.apache.tapestry5.services.Session;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Base class for strategies that store their values as keys in the session. Implements a uniform format for the keys,
 * based on a prefix to identify the particular strategy.
 */
public abstract class AbstractSessionPersistentFieldStrategy implements PersistentFieldStrategy
{
    private final String prefix;

    private final Request request;

    protected AbstractSessionPersistentFieldStrategy(String prefix, Request request)
    {
        this.prefix = prefix;
        this.request = request;
    }

    public final Collection<PersistentFieldChange> gatherFieldChanges(String pageName)
    {
        Session session = request.getSession(false);

        if (session == null) return Collections.emptyList();

        List<PersistentFieldChange> result = newList();

        String fullPrefix = prefix + pageName + ":";

        for (String name : session.getAttributeNames(fullPrefix))
        {
            Object persistedValue = session.getAttribute(name);

            Object applicationValue = persistedValue == null ? null : convertPersistedToApplicationValue(
                    persistedValue);

            PersistentFieldChange change = buildChange(name, applicationValue);

            result.add(change);

            didReadChange(session, name);
        }

        return result;
    }

    public void discardChanges(String pageName)
    {
        Session session = request.getSession(false);

        if (session == null) return;

        String fullPrefix = prefix + pageName + ":";

        for (String name : session.getAttributeNames(fullPrefix))
        {
            session.setAttribute(name, null);
        }
    }

    /**
     * Called after each key is read by {@link #gatherFieldChanges(String)}. This implementation does nothing,
     * subclasses may override.
     *
     * @param session       the session from which a value was just read
     * @param attributeName the name of the attribute used to read a value
     */
    protected void didReadChange(Session session, String attributeName)
    {
    }

    private PersistentFieldChange buildChange(String name, Object newValue)
    {
        String[] chunks = name.split(":");

        // Will be empty string for the root component
        String componentId = chunks[2];
        String fieldName = chunks[3];

        return new PersistentFieldChangeImpl(componentId, fieldName, newValue);
    }

    public final void postChange(String pageName, String componentId, String fieldName,
                                 Object newValue)
    {
        notBlank(pageName, "pageName");
        notBlank(fieldName, "fieldName");

        Object persistedValue = newValue == null ? null : convertApplicationValueToPersisted(newValue);

        StringBuilder builder = new StringBuilder(prefix);
        builder.append(pageName);
        builder.append(':');

        if (componentId != null) builder.append(componentId);

        builder.append(':');
        builder.append(fieldName);

        Session session = request.getSession(persistedValue != null);

        // TAPESTRY-2308: The session will be false when newValue is null and the session
        // does not already exist.

        if (session != null)
        {
            session.setAttribute(builder.toString(), persistedValue);
        }
    }

    /**
     * Hook that allows a value to be converted as it is written to the session. Passed the new value provided by the
     * application, returns the object to be stored in the session. This implementation simply returns the provided
     * value.
     *
     * @param newValue non-null value
     * @return persisted value
     * @see #convertPersistedToApplicationValue(Object)
     */
    protected Object convertApplicationValueToPersisted(Object newValue)
    {
        return newValue;
    }

    /**
     * Converts a persisted value stored in the session back into an application value.   This implementation returns
     * the persisted value as is.
     *
     * @param persistedValue non-null persisted value
     * @return application value
     * @see #convertPersistedToApplicationValue(Object)
     */
    protected Object convertPersistedToApplicationValue(Object persistedValue)
    {
        return persistedValue;
    }
}
