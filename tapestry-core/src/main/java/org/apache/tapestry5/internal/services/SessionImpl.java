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

import org.apache.tapestry5.ioc.internal.util.CollectionFactory;
import org.apache.tapestry5.ioc.internal.util.InternalUtils;
import org.apache.tapestry5.services.Session;
import org.apache.tapestry5.services.SessionPersistedObjectAnalyzer;

import javax.servlet.http.HttpSession;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;

/**
 * A thin wrapper around {@link HttpSession}.
 */
public class SessionImpl implements Session
{
    private final SessionPersistedObjectAnalyzer analyzer;

    private final HttpSession session;

    private boolean invalidated = false;

    /**
     * Cache of attribute objects read from, or written to, the real session. This is needed for end-of-request
     * processing.
     */
    private final Map<String, Object> sessionAttributeCache = CollectionFactory.newMap();

    public SessionImpl(HttpSession session, SessionPersistedObjectAnalyzer analyzer)
    {
        this.session = session;
        this.analyzer = analyzer;
    }

    public Object getAttribute(String name)
    {
        Object result = session.getAttribute(name);

        sessionAttributeCache.put(name, result);

        return result;
    }

    public List<String> getAttributeNames()
    {
        return InternalUtils.toList(session.getAttributeNames());
    }

    public void setAttribute(String name, Object value)
    {
        session.setAttribute(name, value);

        sessionAttributeCache.put(name, value);
    }

    public List<String> getAttributeNames(String prefix)
    {
        List<String> result = CollectionFactory.newList();

        Enumeration e = session.getAttributeNames();
        while (e.hasMoreElements())
        {
            String name = (String) e.nextElement();

            if (name.startsWith(prefix)) result.add(name);
        }

        Collections.sort(result);

        return result;
    }

    public int getMaxInactiveInterval()
    {
        return session.getMaxInactiveInterval();
    }

    public void invalidate()
    {
        invalidated = true;

        session.invalidate();

        sessionAttributeCache.clear();
    }

    public boolean isInvalidated()
    {
        return invalidated;
    }

    public void setMaxInactiveInterval(int seconds)
    {
        session.setMaxInactiveInterval(seconds);
    }

    public void restoreDirtyObjects()
    {
        if (invalidated) return;

        if (sessionAttributeCache.isEmpty()) return;

        for (Map.Entry<String, Object> entry : sessionAttributeCache.entrySet())
        {
            String attributeName = entry.getKey();

            Object attributeValue = entry.getValue();

            if (attributeValue == null)
                continue;

            if (analyzer.isDirty(attributeValue))
                session.setAttribute(attributeName, attributeValue);
        }
    }
}
