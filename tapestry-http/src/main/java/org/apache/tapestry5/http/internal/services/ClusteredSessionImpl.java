//  Copyright 2011, 2013, 2020 The Apache Software Foundation
//
//  Licensed under the Apache License, Version 2.0 (the "License");
//  you may not use this file except in compliance with the License.
//  You may obtain a copy of the License at
//
//  http://www.apache.org/licenses/LICENSE-2.0
//
//  Unless required by applicable law or agreed to in writing, software
//  distributed under the License is distributed on an "AS IS" BASIS,
//  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//  See the License for the specific language governing permissions and
//  limitations under the License.

package org.apache.tapestry5.http.internal.services;

import org.apache.tapestry5.commons.util.CollectionFactory;
import org.apache.tapestry5.http.services.SessionPersistedObjectAnalyzer;
import org.apache.tapestry5.ioc.services.PerthreadManager;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Map;

/**
 * A thin wrapper around {@link javax.servlet.http.HttpSession} that supports re-storing of mutable
 * session attributes at the end of the request (see {@link #restoreDirtyObjects()}). This is only
 * used when {@linkplain org.apache.tapestry5.SymbolConstants#CLUSTERED_SESSIONS clustering}.
 *
 * @since 5.3
 * @see SessionPersistedObjectAnalyzer
 */
public class ClusteredSessionImpl extends SessionImpl
{
    private final SessionPersistedObjectAnalyzer analyzer;

    /**
     * Cache of attribute objects read from, or written to, the real session.
     * This is needed for end-of-request
     * processing.
     */
    private final Map<String, Object> sessionAttributeCache = CollectionFactory.newMap();

    public ClusteredSessionImpl(HttpServletRequest request,
            HttpSession session,
            SessionLock lock, SessionPersistedObjectAnalyzer analyzer)
    {
        super(request, session, lock);

        this.analyzer = analyzer;
    }

    @Override
    public Object getAttribute(String name)
    {
        Object result = super.getAttribute(name);

        sessionAttributeCache.put(name, result);

        return result;
    }

    public void setAttribute(String name, Object value)
    {
        super.setAttribute(name, value);

        sessionAttributeCache.put(name, value);
    }

    public void invalidate()
    {
        super.invalidate();

        sessionAttributeCache.clear();
    }

    public void restoreDirtyObjects()
    {
        if (isInvalidated())
        {
            return;
        }

        if (sessionAttributeCache.isEmpty())
        {
            return;
        }

        for (Map.Entry<String, Object> entry : sessionAttributeCache.entrySet())
        {
            String attributeName = entry.getKey();

            Object attributeValue = entry.getValue();

            if (attributeValue == null)
            {
                continue;
            }

            if (analyzer.checkAndResetDirtyState(attributeValue))
            {
                super.setAttribute(attributeName, attributeValue);
            }
        }
    }
}
