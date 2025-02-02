// Copyright 2006-2013 The Apache Software Foundation
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

package org.apache.tapestry5.http.internal.services;

import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Objects;

import org.apache.tapestry5.commons.util.CollectionFactory;
import org.apache.tapestry5.http.services.Session;
import org.apache.tapestry5.ioc.internal.util.InternalUtils;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

/**
 * A thin wrapper around {@link HttpSession}.
 */
public class SessionImpl implements Session
{
    private final HttpServletRequest request;

    private final HttpSession session;

    private boolean invalidated = false;

    private final SessionLock lock;

    public SessionImpl(HttpServletRequest request, HttpSession session, SessionLock lock)
    {
        this.request = request;
        this.session = session;
        this.lock = lock;
    }

    @Override
    public Object getAttribute(String name)
    {
        return getAttribute(name, Session.LockMode.WRITE);
    }

    @Override
    public Object getAttribute(String name, Session.LockMode lockMode)
    {
        Objects.requireNonNull(name, "name must be non-null");

        // If a WRITE lock is requested, check first if the key exists
        // to prevent a lock upgrade if not necessary.
        if (lockMode == null || lockMode == Session.LockMode.WRITE)
        {
            if (!containsAttribute(name)) return null;
        }

        acquireLock(lockMode, Session.LockMode.WRITE);

        return session.getAttribute(name);
    }

    @Override
    public List<String> getAttributeNames()
    {
        return getAttributeNames(Session.LockMode.READ);
    }

    @Override
    public List<String> getAttributeNames(Session.LockMode lockMode)
    {
        acquireLock(lockMode, Session.LockMode.READ);

        return InternalUtils.toList(session.getAttributeNames());
    }

    @Override
    public void setAttribute(String name, Object value)
    {
        Objects.requireNonNull(name, "name must be non-null");

        lock.acquireWriteLock();

        session.setAttribute(name, value);
    }

    @Override
    public boolean containsAttribute(String name)
    {
        return containsAttribute(name, Session.LockMode.READ);
    }

    @Override
    public boolean containsAttribute(String name, Session.LockMode lockMode)
    {
        Objects.requireNonNull(name, "name must be non-null");

        acquireLock(lockMode, Session.LockMode.READ);

        Enumeration<String> e = session.getAttributeNames();
        while (e.hasMoreElements())
        {
            String attrName = e.nextElement();
            if (attrName.equals(name)) return true;
        }

        return false;
    }

    @Override
    public List<String> getAttributeNames(String prefix)
    {
        return getAttributeNames(prefix, Session.LockMode.READ);
    }

    @Override
    public List<String> getAttributeNames(String prefix, Session.LockMode lockMode)
    {
        Objects.requireNonNull(prefix, "prefix must be non-null");

        acquireLock(lockMode, Session.LockMode.READ);

        List<String> result = CollectionFactory.newList();

        Enumeration<String> e = session.getAttributeNames();
        while (e.hasMoreElements())
        {
            String name = e.nextElement();

            if (name.startsWith(prefix)) result.add(name);
        }

        Collections.sort(result);

        return result;
    }

    @Override
    public int getMaxInactiveInterval()
    {
        return session.getMaxInactiveInterval();
    }

    @Override
    public void invalidate()
    {
        invalidated = true;

        session.invalidate();
    }

    @Override
    public boolean isInvalidated()
    {
        if (invalidated) return true;

        // The easy case is when the session was invalidated through the Tapestry Session
        // object. The hard case is when the HttpSession was invalidated outside of Tapestry,
        // in which case, request.getSession() will return a new HttpSession instance (or null)

        invalidated = request.getSession(false) != session;

        return invalidated;
    }

    @Override
    public void setMaxInactiveInterval(int seconds)
    {
        session.setMaxInactiveInterval(seconds);
    }

    @Override
    public void restoreDirtyObjects()
    {

    }

    private void acquireLock(Session.LockMode requestedMode, Session.LockMode defaultMode) {
        if (requestedMode == null)
        {
            requestedMode = defaultMode;
        }

        switch (requestedMode)
        {
            case NONE:
                break;
            case READ:
                this.lock.acquireReadLock();
                break;
            case WRITE:
                this.lock.acquireWriteLock();
                break;
        }
    }
}
