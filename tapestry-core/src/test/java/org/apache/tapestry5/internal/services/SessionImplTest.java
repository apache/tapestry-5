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

import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import org.apache.tapestry5.http.internal.services.ClusteredSessionImpl;
import org.apache.tapestry5.http.internal.services.SessionImpl;
import org.apache.tapestry5.http.internal.services.SessionLock;
import org.apache.tapestry5.http.services.Session;
import org.apache.tapestry5.http.services.SessionPersistedObjectAnalyzer;
import org.apache.tapestry5.internal.test.InternalBaseTestCase;
import org.testng.annotations.Test;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

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
    public void get_attribute_names_lock_mode()
    {
        Enumeration e = Collections.enumeration(Arrays.asList("fred", "barney"));
        HttpSession hs = mockHttpSession();
        SessionLock lock = mockLock();

        lock.acquireWriteLock();
        expect(hs.getAttributeNames()).andReturn(e);

        replay();

        Session session = new SessionImpl(null, hs, lock);

        assertEquals(session.getAttributeNames(Session.LockMode.WRITE), Arrays.asList("barney", "fred"));

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
    public void contains_attribute()
    {
        List<String> keys = Arrays.asList("fred", "barney");
        HttpSession hs = mockHttpSession();
        SessionLock lock = mockLock();

        // We need one per assert, and Enumerations are exhausted on use
        lock.acquireReadLock();
        expect(hs.getAttributeNames()).andReturn(Collections.enumeration(keys));
        lock.acquireReadLock();
        expect(hs.getAttributeNames()).andReturn(Collections.enumeration(keys));
        lock.acquireReadLock();
        expect(hs.getAttributeNames()).andReturn(Collections.enumeration(keys));
        replay();

        Session session = new SessionImpl(null, hs, lock);

        assertTrue(session.containsAttribute("barney"));
        assertTrue(session.containsAttribute("fred"));
        assertFalse(session.containsAttribute("wilma"));

        verify();
    }

    @Test
    public void get_attribute_write_lock()
    {
        Enumeration e = Collections.enumeration(Arrays.asList("fred", "barney"));
        HttpSession hs = mockHttpSession();
        SessionLock lock = mockLock();

        lock.acquireReadLock();
        lock.acquireWriteLock();
        expect(hs.getAttributeNames()).andReturn(e);
        expect(hs.getAttribute("fred")).andReturn("1");

        replay();

        Session session = new SessionImpl(null, hs, lock);

        assertEquals(session.getAttribute("fred"), "1");

        verify();
    }

    @Test
    public void get_attribute_no_lock_update()
    {
        Enumeration e = Collections.enumeration(Arrays.asList("fred", "barney"));
        HttpSession hs = mockHttpSession();
        SessionLock lock = mockLock();

        lock.acquireReadLock();
        expect(hs.getAttributeNames()).andReturn(e);

        replay();

        Session session = new SessionImpl(null, hs, lock);

        assertEquals(session.getAttribute("wilma"), null);

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

        // TAP5-2799: To reduce write locks, first, a read-lock attempt is done
        // to check if the attribute exists, and only then, a write-lock is acquired.

        lock.acquireReadLock();

        expect(hs.getAttributeNames()).andReturn(Collections.enumeration(Arrays.asList("dirty")));

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
