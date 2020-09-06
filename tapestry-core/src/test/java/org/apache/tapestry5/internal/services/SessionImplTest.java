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

import org.apache.tapestry5.http.internal.services.ClusteredSessionImpl;
import org.apache.tapestry5.http.internal.services.SessionImpl;
import org.apache.tapestry5.http.internal.services.SessionLock;
import org.apache.tapestry5.http.services.Session;
import org.apache.tapestry5.http.services.SessionPersistedObjectAnalyzer;
import org.apache.tapestry5.internal.test.InternalBaseTestCase;
import org.testng.annotations.Test;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.concurrent.locks.ReentrantLock;

public class SessionImplTest extends InternalBaseTestCase
{
    private SessionLock mockLock()
    {
        return newMock(SessionLock.class);
    }

    @Test
    public void get_attribute_names()
    {
        Enumeration e = Collections.enumeration(Arrays.asList("fred", "barney"));
        HttpSession hs = mockHttpSession();
        SessionLock lock = mockLock();

        lock.acquireReadLock();
        expect(hs.getAttributeNames()).andReturn(e);

        replay();

        Session session = new SessionImpl(null, hs, lock);

        assertEquals(session.getAttributeNames(), Arrays.asList("barney", "fred"));

        verify();
    }

    @Test
    public void get_attribute_names_by_prefix()
    {
        Enumeration e = Collections.enumeration(Arrays.asList("fred", "barney", "fanny"));
        HttpSession hs = mockHttpSession();
        SessionLock lock = mockLock();

        lock.acquireReadLock();

        expect(hs.getAttributeNames()).andReturn(e);

        replay();

        Session session = new SessionImpl(null, hs, lock);

        assertEquals(session.getAttributeNames("f"), Arrays.asList("fanny", "fred"));

        verify();
    }

    @Test
    public void invalidate()
    {
        HttpSession hs = mockHttpSession();
        SessionLock lock = mockLock();

        hs.invalidate();

        replay();

        Session session = new SessionImpl(null, hs, lock);

        session.invalidate();

        verify();
    }

    @Test
    public void http_session_invalidate()
    {
        HttpSession hs = mockHttpSession();
        HttpServletRequest hsr = mockHttpServletRequest();
        SessionLock lock = mockLock();

        train_getSession(hsr, false, hs);

        replay();

        Session session = new SessionImpl(hsr, hs, lock);

        assertFalse(session.isInvalidated());

        verify();

        train_getSession(hsr, false, null);

        replay();

        assertTrue(session.isInvalidated());

        verify();

        train_getSession(hsr, false, mockHttpSession());

        replay();

        assertTrue(session.isInvalidated());

    }

    @Test
    public void set_max_inactive()
    {
        HttpSession hs = mockHttpSession();
        int seconds = 999;

        hs.setMaxInactiveInterval(seconds);

        replay();

        Session session = new SessionImpl(null, hs, null);

        session.setMaxInactiveInterval(seconds);

        verify();
    }

    @Test
    public void get_max_inactive()
    {
        HttpSession hs = mockHttpSession();

        int seconds = 999;

        expect(hs.getMaxInactiveInterval()).andReturn(seconds);

        replay();

        Session session = new SessionImpl(null, hs, null);

        assertEquals(session.getMaxInactiveInterval(), seconds);

        verify();
    }

    @Test
    public void dirty_persisted_object_is_forced_to_update()
    {
        HttpSession hs = mockHttpSession();
        HttpServletRequest hsr = mockHttpServletRequest();
        SessionPersistedObjectAnalyzer analyzer = newMock(SessionPersistedObjectAnalyzer.class);
        Object dirty = new Object();
        SessionLock lock = mockLock();

        lock.acquireWriteLock();

        train_getAttribute(hs, "dirty", dirty);

        replay();

        Session session = new ClusteredSessionImpl(hsr, hs, lock, analyzer);

        assertSame(session.getAttribute("dirty"), dirty);

        verify();

        expect(analyzer.checkAndResetDirtyState(dirty)).andReturn(true);

        train_getSession(hsr, false, hs);

        lock.acquireWriteLock();

        hs.setAttribute("dirty", dirty);

        replay();

        session.restoreDirtyObjects();

        verify();
    }
}
