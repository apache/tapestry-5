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

import org.apache.tapestry5.internal.test.InternalBaseTestCase;
import org.apache.tapestry5.internal.transform.pages.ReadOnlyBean;
import org.apache.tapestry5.services.ApplicationStateCreator;
import org.apache.tapestry5.services.ApplicationStatePersistenceStrategy;
import org.apache.tapestry5.services.Request;
import org.apache.tapestry5.services.Session;
import org.testng.annotations.Test;

public class SessionApplicationStatePersistenceStrategyTest extends InternalBaseTestCase
{
    @SuppressWarnings("unchecked")
    @Test
    public void get_aso_already_exists()
    {
        Request request = mockRequest();
        Session session = mockSession();
        Class asoClass = ReadOnlyBean.class;
        Object aso = new ReadOnlyBean();
        String key = "sso:" + asoClass.getName();
        ApplicationStateCreator creator = mockApplicationStateCreator();

        train_getSession(request, true, session);
        train_getAttribute(session, key, aso);

        replay();

        ApplicationStatePersistenceStrategy strategy = new SessionApplicationStatePersistenceStrategy(request);

        assertSame(strategy.get(asoClass, creator), aso);

        verify();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void check_exists_does_not_create_session()
    {
        Request request = mockRequest();
        Class asoClass = ReadOnlyBean.class;

        train_getSession(request, false, null);

        replay();

        ApplicationStatePersistenceStrategy strategy = new SessionApplicationStatePersistenceStrategy(request);

        assertFalse(strategy.exists(asoClass));

        verify();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void get_aso_needs_to_be_created()
    {
        Request request = mockRequest();
        Session session = mockSession();
        Class asoClass = ReadOnlyBean.class;
        Object aso = new ReadOnlyBean();
        String key = "sso:" + asoClass.getName();
        ApplicationStateCreator creator = mockApplicationStateCreator();

        // First for exists()
        train_getSession(request, false, session);
        train_getAttribute(session, key, null);

        // Second for get()
        train_getSession(request, true, session);

        // Not in map
        train_getAttribute(session, key, null);

        train_create(creator, aso);

        session.setAttribute(key, aso);

        // Then for exists() after
        train_getSession(request, false, session);
        train_getAttribute(session, key, aso);

        replay();

        ApplicationStatePersistenceStrategy strategy = new SessionApplicationStatePersistenceStrategy(request);

        assertFalse(strategy.exists(asoClass));

        assertSame(strategy.get(asoClass, creator), aso);

        assertTrue(strategy.exists(asoClass));

        verify();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void set_aso()
    {
        Request request = mockRequest();
        Session session = mockSession();
        Class asoClass = ReadOnlyBean.class;
        Object aso = new ReadOnlyBean();
        String key = "sso:" + asoClass.getName();

        train_getSession(request, true, session);
        session.setAttribute(key, aso);

        replay();

        ApplicationStatePersistenceStrategy strategy = new SessionApplicationStatePersistenceStrategy(request);

        strategy.set(asoClass, aso);

        verify();
    }
}
