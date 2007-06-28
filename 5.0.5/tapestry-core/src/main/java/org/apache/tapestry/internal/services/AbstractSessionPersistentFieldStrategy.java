// Copyright 2007 The Apache Software Foundation
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

import static org.apache.tapestry.ioc.internal.util.CollectionFactory.newList;
import static org.apache.tapestry.ioc.internal.util.Defense.notBlank;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.tapestry.services.PersistentFieldChange;
import org.apache.tapestry.services.PersistentFieldStrategy;
import org.apache.tapestry.services.Request;
import org.apache.tapestry.services.Session;

/**
 * Base class for strategies that store their values as keys in the session. Implements a uniform
 * format for the keys, based on a prefix to identify the particular strategy.
 */
public abstract class AbstractSessionPersistentFieldStrategy implements PersistentFieldStrategy
{
    private final String _prefix;

    private final Request _request;

    protected AbstractSessionPersistentFieldStrategy(String prefix, Request request)
    {
        _prefix = prefix;
        _request = request;
    }

    public final Collection<PersistentFieldChange> gatherFieldChanges(String pageName)
    {
        Session session = _request.getSession(false);

        if (session == null) return Collections.emptyList();

        List<PersistentFieldChange> result = newList();

        String fullPrefix = _prefix + pageName + ":";

        for (String name : session.getAttributeNames(fullPrefix))
        {
            PersistentFieldChange change = buildChange(name, session.getAttribute(name));

            result.add(change);

            didReadChange(session, name);
        }

        return result;
    }

    /**
     * Called after each key is read by {@link #gatherFieldChanges(String)}. This implementation
     * does nothing, subclasses may override.
     * 
     * @param session
     *            the session from which a value was just read
     * @param attributeName
     *            the name of the attribute used to read a value
     */
    protected void didReadChange(Session session, String attributeName)
    {
    }

    private PersistentFieldChange buildChange(String name, Object attribute)
    {
        // TODO: Regexp is probably too expensive for what we need here. Maybe an IOC InternalUtils
        // method for this purpose?

        String[] chunks = name.split(":");

        // Will be empty string for the root component
        String componentId = chunks[2];
        String fieldName = chunks[3];

        return new PersistentFieldChangeImpl(componentId, fieldName, attribute);
    }

    public final void postChange(String pageName, String componentId, String fieldName,
            Object newValue)
    {
        notBlank(pageName, "pageName");
        notBlank(fieldName, "fieldName");

        StringBuilder builder = new StringBuilder(_prefix);
        builder.append(pageName);
        builder.append(':');

        if (componentId != null) builder.append(componentId);

        builder.append(':');
        builder.append(fieldName);

        Session session = _request.getSession(true);

        session.setAttribute(builder.toString(), newValue);
    }
}
