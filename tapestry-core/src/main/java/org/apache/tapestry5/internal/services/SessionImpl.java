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

package org.apache.tapestry5.internal.services;

import org.apache.tapestry5.ioc.internal.util.CollectionFactory;
import org.apache.tapestry5.ioc.internal.util.InternalUtils;
import org.apache.tapestry5.ioc.services.PerthreadManager;
import org.apache.tapestry5.services.Session;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

/**
 * A thin wrapper around {@link HttpSession}.
 */
public class SessionImpl implements Session
{
    private final HttpServletRequest request;

    private final HttpSession session;

    private boolean invalidated = false;

    private final PerthreadManager perthreadManager;

    private final ReentrantLock lock;

    final static String LOCK_KEY = "org.apache.tapestry5.SessionLock";

    public SessionImpl(HttpServletRequest request, HttpSession session, PerthreadManager perthreadManager)
    {
        this.request = request;
        this.session = session;
        this.perthreadManager = perthreadManager;

        lock = findOrCreateLock();
    }

    private ReentrantLock findOrCreateLock()
    {

        // Yes, this itself is a problem as multiple threads may attempt to create the HttpSession simultaneously.
        // I suspect that is quite rare however.

        ReentrantLock result = (ReentrantLock) session.getAttribute(LOCK_KEY);

        if (result == null)
        {
            result = new ReentrantLock();
            session.setAttribute(LOCK_KEY, result);
        }

        return result;
    }

    /**
     * Gains the exclusive lock needed to perform any access to attributes inside the session. The lock is acquired
     * on any read or write access, and is held until the request completes.
     */
    private void lock()
    {
        if (!lock.isLocked())
        {
            // The HttpSession may be shared across threads, but the lock (almost) certainly is.
            lock.lock();

            perthreadManager.addThreadCleanupCallback(new Runnable()
            {
                public void run()
                {
                    lock.unlock();
                }
            });
        }
    }

    public Object getAttribute(String name)
    {
        lock();

        return session.getAttribute(name);
    }

    public List<String> getAttributeNames()
    {
        lock();

        return InternalUtils.toList(session.getAttributeNames());
    }

    public void setAttribute(String name, Object value)
    {
        lock();

        session.setAttribute(name, value);
    }

    public List<String> getAttributeNames(String prefix)
    {
        lock();

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
    }

    public boolean isInvalidated()
    {
        if (invalidated) return true;

        // The easy case is when the session was invalidated through the Tapestry Session
        // object. The hard case is when the HttpSession was invalidated outside of Tapestry,
        // in which case, request.getSession() will return a new HttpSession instance (or null)

        invalidated = request.getSession(false) != session;

        return invalidated;
    }

    public void setMaxInactiveInterval(int seconds)
    {
        session.setMaxInactiveInterval(seconds);
    }

    public void restoreDirtyObjects()
    {

    }
}
