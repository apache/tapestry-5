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

import org.apache.tapestry5.internal.test.InternalBaseTestCase;
import org.apache.tapestry5.internal.util.Holder;
import org.apache.tapestry5.ioc.services.PerthreadManager;
import org.apache.tapestry5.services.Request;
import org.apache.tapestry5.services.Session;
import org.apache.tapestry5.services.SessionPersistedObjectAnalyzer;
import org.easymock.EasyMock;
import org.easymock.IAnswer;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.concurrent.locks.ReentrantLock;

public class SessionImplTest extends InternalBaseTestCase
{
    private PerthreadManager perThreadManager;

    private final ReentrantLock lock = new ReentrantLock();

    @BeforeClass
    public void setup()
    {
        perThreadManager = getService(PerthreadManager.class);
    }

    @AfterMethod
    public void releaseLock()
    {
        perThreadManager.cleanup();

        assertFalse(lock.isLocked());
    }

    void trainForLock(HttpSession session)
    {
        expect(session.getAttribute(SessionImpl.LOCK_KEY)).andReturn(lock);
    }

    @Test
    public void will_create_lock_if_not_present()
    {
        HttpSession hs = mockHttpSession();

        final Holder<ReentrantLock> holder = Holder.create();

        expect(hs.getAttribute(SessionImpl.LOCK_KEY)).andReturn(null);

        hs.setAttribute(EasyMock.eq(SessionImpl.LOCK_KEY), EasyMock.isA(ReentrantLock.class));
        EasyMock.expectLastCall().andStubAnswer(new IAnswer<Object>()
        {
            public Object answer() throws Throwable
            {
                ReentrantLock lock = (ReentrantLock) EasyMock.getCurrentArguments()[1];

                holder.put(lock);

                return null;
            }
        });

        replay();

        new SessionImpl(null, hs, perThreadManager);

        assertFalse(holder.get().isLocked());

        verify();
    }

    @Test
    public void get_attribute_names()
    {
        Enumeration e = Collections.enumeration(Arrays.asList("fred", "barney"));
        HttpSession hs = mockHttpSession();

        trainForLock(hs);

        expect(hs.getAttributeNames()).andReturn(e);

        replay();

        Session session = new SessionImpl(null, hs, perThreadManager);

        assertEquals(session.getAttributeNames(), Arrays.asList("barney", "fred"));

        assertTrue(lock.isLocked());

        verify();
    }

    @Test
    public void get_attribute_names_by_prefix()
    {
        Enumeration e = Collections.enumeration(Arrays.asList("fred", "barney", "fanny"));
        HttpSession hs = mockHttpSession();

        trainForLock(hs);

        expect(hs.getAttributeNames()).andReturn(e);

        replay();

        Session session = new SessionImpl(null, hs, perThreadManager);

        assertEquals(session.getAttributeNames("f"), Arrays.asList("fanny", "fred"));

        assertTrue(lock.isLocked());

        verify();
    }

    @Test
    public void invalidate()
    {
        HttpSession hs = mockHttpSession();

        trainForLock(hs);

        hs.invalidate();

        replay();

        Session session = new SessionImpl(null, hs, perThreadManager);

        session.invalidate();

        verify();
    }

    @Test
    public void http_session_invalidate()
    {
        HttpSession hs = mockHttpSession();
        HttpServletRequest hsr = mockHttpServletRequest();

        train_getSession(hsr, false, hs);
        trainForLock(hs);

        replay();

        Session session = new SessionImpl(hsr, hs, perThreadManager);

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
        trainForLock(hs);

        hs.setMaxInactiveInterval(seconds);

        replay();

        Session session = new SessionImpl(null, hs, perThreadManager);

        session.setMaxInactiveInterval(seconds);

        verify();
    }

    @Test
    public void get_max_inactive()
    {
        HttpSession hs = mockHttpSession();

        trainForLock(hs);

        int seconds = 999;

        expect(hs.getMaxInactiveInterval()).andReturn(seconds);

        replay();

        Session session = new SessionImpl(null, hs, perThreadManager);

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

        trainForLock(hs);
        train_getAttribute(hs, "dirty", dirty);

        replay();

        Session session = new ClusteredSessionImpl(hsr, hs, perThreadManager, analyzer);

        assertSame(session.getAttribute("dirty"), dirty);

        verify();

        expect(analyzer.checkAndResetDirtyState(dirty)).andReturn(true);

        train_getSession(hsr, false, hs);

        hs.setAttribute("dirty", dirty);

        replay();

        session.restoreDirtyObjects();

        verify();
    }
}
