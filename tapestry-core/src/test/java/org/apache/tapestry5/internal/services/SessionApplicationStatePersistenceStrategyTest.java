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

import org.apache.tapestry5.OptimizedApplicationStateObject;
import org.apache.tapestry5.internal.events.EndOfRequestListener;
import org.apache.tapestry5.internal.test.InternalBaseTestCase;
import org.apache.tapestry5.internal.transform.pages.ReadOnlyBean;
import org.apache.tapestry5.ioc.internal.util.CollectionFactory;
import org.apache.tapestry5.services.ApplicationStateCreator;
import org.apache.tapestry5.services.ApplicationStatePersistenceStrategy;
import org.apache.tapestry5.services.Request;
import org.apache.tapestry5.services.Session;
import org.easymock.Capture;
import static org.easymock.EasyMock.capture;
import static org.easymock.EasyMock.eq;
import org.testng.annotations.Test;

import java.util.Collections;
import java.util.Map;

public class SessionApplicationStatePersistenceStrategyTest extends InternalBaseTestCase
{
    private static final String ASO_MAP_ATTRIBUTE = SessionApplicationStatePersistenceStrategy.ASO_MAP_ATTRIBUTE;

    @SuppressWarnings("unchecked")
    @Test
    public void get_aso_already_exists()
    {
        Request request = mockRequest();
        Session session = mockSession();
        Class asoClass = ReadOnlyBean.class;
        Object aso = new ReadOnlyBean();
        String key = "aso:" + asoClass.getName();
        ApplicationStateCreator creator = mockApplicationStateCreator();
        Map<String, Object> asoMap = CollectionFactory.newMap();

        train_getSession(request, true, session);
        train_getAttribute(session, key, aso);

        train_getAttribute(request, ASO_MAP_ATTRIBUTE, asoMap);

        replay();

        ApplicationStatePersistenceStrategy strategy = new SessionApplicationStatePersistenceStrategy(request);

        assertSame(strategy.get(asoClass, creator), aso);

        // Check that the ASO Map was updated.

        assertSame(asoMap.get(key), aso);

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
        String key = "aso:" + asoClass.getName();
        ApplicationStateCreator creator = mockApplicationStateCreator();
        Map<String, Object> asoMap = CollectionFactory.newMap();

        // First for exists()
        train_getSession(request, false, session);
        train_getAttribute(session, key, null);

        // Second for get()
        train_getSession(request, true, session);

        train_getAttribute(request, ASO_MAP_ATTRIBUTE, asoMap);
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

        assertSame(asoMap.get(key), aso);

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
        String key = "aso:" + asoClass.getName();
        Map<String, Object> asoMap = CollectionFactory.newMap();

        train_getSession(request, true, session);
        session.setAttribute(key, aso);

        train_getAttribute(request, ASO_MAP_ATTRIBUTE, asoMap);

        replay();

        ApplicationStatePersistenceStrategy strategy = new SessionApplicationStatePersistenceStrategy(request);

        strategy.set(asoClass, aso);

        assertSame(asoMap.get(key), aso);

        verify();
    }

    @Test
    public void aso_map_created_as_needed()
    {
        Request request = mockRequest();
        Session session = mockSession();
        Class asoClass = ReadOnlyBean.class;
        Object aso = new ReadOnlyBean();
        String key = "aso:" + asoClass.getName();
        Capture<Map<String, Object>> asoMapCapture = newCapture();

        train_getSession(request, true, session);
        session.setAttribute(key, aso);

        train_getAttribute(request, ASO_MAP_ATTRIBUTE, null);

        request.setAttribute(eq(ASO_MAP_ATTRIBUTE), capture(asoMapCapture));

        replay();

        ApplicationStatePersistenceStrategy strategy = new SessionApplicationStatePersistenceStrategy(request);

        strategy.set(asoClass, aso);

        assertSame(asoMapCapture.getValue().get(key), aso);

        verify();
    }

    @Test
    public void restore_map_is_empty()
    {
        Request request = mockRequest();
        Map<String, Object> asoMap = Collections.emptyMap();

        train_getAttribute(request, ASO_MAP_ATTRIBUTE, asoMap);

        replay();

        EndOfRequestListener strategy = new SessionApplicationStatePersistenceStrategy(request);

        strategy.requestDidComplete();

        verify();
    }

    @Test
    public void restore_aso_is_null()
    {
        Request request = mockRequest();
        Map<String, Object> asoMap = CollectionFactory.newMap();

        asoMap.put("some.key", null);

        train_getAttribute(request, ASO_MAP_ATTRIBUTE, asoMap);

        replay();

        EndOfRequestListener strategy = new SessionApplicationStatePersistenceStrategy(request);

        strategy.requestDidComplete();

        verify();
    }


    @Test
    public void restore_non_optimized_object()
    {
        Request request = mockRequest();
        Session session = mockSession();
        Map<String, Object> asoMap = CollectionFactory.newMap();

        String key = "foo:bar";
        Object aso = new Object();

        asoMap.put(key, aso);

        train_getAttribute(request, ASO_MAP_ATTRIBUTE, asoMap);
        train_getSession(request, true, session);
        session.setAttribute(key, aso);

        replay();

        EndOfRequestListener strategy = new SessionApplicationStatePersistenceStrategy(request);

        strategy.requestDidComplete();

        verify();
    }

    @Test
    public void restore_optimized_object_is_dirty()
    {
        Request request = mockRequest();
        Session session = mockSession();
        Map<String, Object> asoMap = CollectionFactory.newMap();

        String key = "foo:bar";
        OptimizedApplicationStateObject aso = mockOptimizedApplicationStateObject(true);

        asoMap.put(key, aso);

        train_getAttribute(request, ASO_MAP_ATTRIBUTE, asoMap);
        train_getSession(request, true, session);
        session.setAttribute(key, aso);

        replay();

        EndOfRequestListener strategy = new SessionApplicationStatePersistenceStrategy(request);

        strategy.requestDidComplete();

        verify();
    }

    @Test
    public void restore_optimized_object_is_clean()
    {
        Request request = mockRequest();
        Session session = mockSession();
        Map<String, Object> asoMap = CollectionFactory.newMap();

        String key = "foo:bar";
        OptimizedApplicationStateObject aso = mockOptimizedApplicationStateObject(false);

        asoMap.put(key, aso);

        train_getAttribute(request, ASO_MAP_ATTRIBUTE, asoMap);

        replay();

        EndOfRequestListener strategy = new SessionApplicationStatePersistenceStrategy(request);

        strategy.requestDidComplete();

        verify();
    }

    private OptimizedApplicationStateObject mockOptimizedApplicationStateObject(boolean dirty)
    {

        OptimizedApplicationStateObject object = newMock(OptimizedApplicationStateObject.class);

        expect(object.isApplicationStateObjectDirty()).andReturn(dirty);

        return object;
    }
}
