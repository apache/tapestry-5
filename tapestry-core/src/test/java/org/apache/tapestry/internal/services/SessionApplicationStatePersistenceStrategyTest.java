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

import org.apache.tapestry.internal.test.InternalBaseTestCase;
import org.apache.tapestry.services.ApplicationStateCreator;
import org.apache.tapestry.services.ApplicationStatePersistenceStrategy;
import org.apache.tapestry.services.Session;
import org.testng.annotations.Test;

public class SessionApplicationStatePersistenceStrategyTest extends InternalBaseTestCase
{
    @SuppressWarnings("unchecked")
    @Test
    public void get_aso_already_exists()
    {
        SessionHolder holder = newMock(SessionHolder.class);
        Session session = newSession();
        Class asoClass = ReadOnlyBean.class;
        Object aso = new ReadOnlyBean();
        String key = "aso:" + asoClass.getName();
        ApplicationStateCreator creator = newApplicationStateCreator();

        train_getSession(holder, true, session);
        train_getAttribute(session, key, aso);

        replay();

        ApplicationStatePersistenceStrategy strategy = new SessionApplicationStatePersistenceStrategy(
                holder);

        assertSame(strategy.get(asoClass, creator), aso);

        verify();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void get_aso_needs_to_be_created()
    {
        SessionHolder holder = newMock(SessionHolder.class);
        Session session = newSession();
        Class asoClass = ReadOnlyBean.class;
        Object aso = new ReadOnlyBean();
        String key = "aso:" + asoClass.getName();
        ApplicationStateCreator creator = newApplicationStateCreator();

        train_getSession(holder, true, session);
        train_getAttribute(session, key, null);

        train_create(creator, aso);

        session.setAttribute(key, aso);

        replay();

        ApplicationStatePersistenceStrategy strategy = new SessionApplicationStatePersistenceStrategy(
                holder);

        assertSame(strategy.get(asoClass, creator), aso);

        verify();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void set_aso()
    {
        SessionHolder holder = newMock(SessionHolder.class);
        Session session = newSession();
        Class asoClass = ReadOnlyBean.class;
        Object aso = new ReadOnlyBean();
        String key = "aso:" + asoClass.getName();

        train_getSession(holder, true, session);
        session.setAttribute(key, aso);

        replay();

        ApplicationStatePersistenceStrategy strategy = new SessionApplicationStatePersistenceStrategy(
                holder);

        strategy.set(asoClass, aso);

        verify();
    }

    protected final void train_getSession(SessionHolder holder, boolean create, Session session)
    {
        expect(holder.getSession(create)).andReturn(session);
    }
}
