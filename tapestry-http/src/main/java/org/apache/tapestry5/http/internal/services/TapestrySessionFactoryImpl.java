//  Copyright 2011, 2013 The Apache Software Foundation
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

import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.tapestry5.http.TapestryHttpSymbolConstants;
import org.apache.tapestry5.http.services.Session;
import org.apache.tapestry5.http.services.SessionPersistedObjectAnalyzer;
import org.apache.tapestry5.ioc.annotations.Symbol;
import org.apache.tapestry5.ioc.services.PerthreadManager;

public class TapestrySessionFactoryImpl implements TapestrySessionFactory
{
    private boolean clustered;

    private final SessionPersistedObjectAnalyzer analyzer;

    private final HttpServletRequest request;

    private final PerthreadManager perthreadManager;

    private final boolean sessionLockingEnabled;

    private final Lock mapLock = new ReentrantLock();

    private final Map<HttpSession, SessionLock> sessionToLock = new WeakHashMap<HttpSession, SessionLock>();

    private final SessionLock NO_OP_LOCK = new SessionLock()
    {
        public void acquireReadLock()
        {
        }

        public void acquireWriteLock()
        {
        }
    };

    private class SessionLockImpl implements SessionLock
    {

        private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

        private boolean isReadLocked()
        {
            return lock.getReadHoldCount() != 0;
        }

        private boolean isWriteLocked()
        {
            return lock.isWriteLockedByCurrentThread();
        }

        public void acquireReadLock()
        {
            if (isReadLocked() || isWriteLocked())
            {
                return;
            }

            lock.readLock().lock();

            perthreadManager.addThreadCleanupCallback(new Runnable()
            {
                public void run()
                {
                    // The read lock may have been released, if upgraded to a write lock.
                    if (isReadLocked())
                    {
                        lock.readLock().unlock();
                    }
                }
            });
        }

        public void acquireWriteLock()
        {
            if (isWriteLocked())
            {
                return;
            }

            if (isReadLocked())
            {
                lock.readLock().unlock();
            }

            // During this window, no lock is held, and the next call may block.

            lock.writeLock().lock();

            perthreadManager.addThreadCleanupCallback(new Runnable()
            {
                public void run()
                {
                    // This is the only way a write lock is unlocked, so no check is needed.
                    lock.writeLock().unlock();
                }
            });
        }
    }

    public TapestrySessionFactoryImpl(
            @Symbol(TapestryHttpSymbolConstants.CLUSTERED_SESSIONS)
            boolean clustered,
            SessionPersistedObjectAnalyzer analyzer,
            HttpServletRequest request,
            PerthreadManager perthreadManager,
            @Symbol(TapestryHttpSymbolConstants.SESSION_LOCKING_ENABLED)
            boolean sessionLockingEnabled)
    {
        this.clustered = clustered;
        this.analyzer = analyzer;
        this.request = request;
        this.perthreadManager = perthreadManager;
        this.sessionLockingEnabled = sessionLockingEnabled;
    }

    public Session getSession(boolean create)
    {
        final HttpSession httpSession = request.getSession(create);

        if (httpSession == null)
        {
            return null;
        }

        SessionLock lock = lockForSession(httpSession);

        if (clustered)
        {
            return new ClusteredSessionImpl(request, httpSession, lock, analyzer);
        }

        return new SessionImpl(request, httpSession, lock);
    }

    private SessionLock lockForSession(HttpSession session)
    {
        if (!sessionLockingEnabled)
        {
            return NO_OP_LOCK;
        }

        // Because WeakHashMap does not look thread safe to me, we use an exclusive
        // lock.
        mapLock.lock();

        try
        {
            SessionLock result = sessionToLock.get(session);

            if (result == null)
            {
                result = new SessionLockImpl();
                sessionToLock.put(session, result);
            }

            return result;
        } finally
        {
            mapLock.unlock();
        }
    }
}
