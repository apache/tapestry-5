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
import org.apache.tapestry5.ioc.internal.util.CollectionFactory;
import org.apache.tapestry5.services.ApplicationStateCreator;
import org.apache.tapestry5.services.ApplicationStatePersistenceStrategy;
import org.apache.tapestry5.services.Request;
import org.apache.tapestry5.services.Session;

import java.util.Map;

/**
 * Stores ASOs in the {@link Session}, which will be created as necessary.
 */
public class SessionApplicationStatePersistenceStrategy implements
        ApplicationStatePersistenceStrategy, EndOfRequestListener
{
    static final String PREFIX = "aso:";

    static final String ASO_MAP_ATTRIBUTE = "org.apache.tapestry.application-state-object-map";

    private final Request request;

    public SessionApplicationStatePersistenceStrategy(Request request)
    {
        this.request = request;
    }

    private Session getSession()
    {
        return request.getSession(true);
    }

    @SuppressWarnings("unchecked")
    public <T> T get(Class<T> asoClass, ApplicationStateCreator<T> creator)
    {
        Session session = getSession();

        String key = buildKey(asoClass);

        Map<String, Object> asoMap = getASOMap();

        T aso = (T) asoMap.get(key);

        if (aso != null) return aso;

        // Otherwise, get/create it in the session and record it in the
        // aso map.

        aso = (T) session.getAttribute(key);

        if (aso == null)
        {
            aso = creator.create();
            session.setAttribute(key, aso);
        }

        asoMap.put(key, aso);

        return aso;
    }

    private <T> String buildKey(Class<T> asoClass)
    {
        return PREFIX + asoClass.getName();
    }

    public <T> void set(Class<T> asoClass, T aso)
    {
        String key = buildKey(asoClass);

        getSession().setAttribute(key, aso);

        getASOMap().put(key, aso);
    }

    public <T> boolean exists(Class<T> asoClass)
    {
        String key = buildKey(asoClass);

        Session session = request.getSession(false);

        return session != null && session.getAttribute(key) != null;
    }

    private Map<String, Object> getASOMap()
    {
        Map<String, Object> result = (Map<String, Object>) request.getAttribute(ASO_MAP_ATTRIBUTE);

        if (result == null)
        {
            result = CollectionFactory.newMap();
            request.setAttribute(ASO_MAP_ATTRIBUTE, result);
        }

        return result;
    }

    public void requestDidComplete()
    {
        Map<String, Object> map = getASOMap();

        for (String key : map.keySet())
        {
            Object aso = map.get(key);

            if (aso == null) continue;

            if (needsRestore(aso))
            {
                Session session = request.getSession(true);

                // It is expected that the ASO implements HttpSessionBindingListener and
                // can clear its dirty flag as it is saved.

                session.setAttribute(key, aso);
            }
        }
    }

    private boolean needsRestore(Object aso)
    {
        // We could check for basic immutable types here, but those are not typically ASOs.
        // ASOs tend to be more complex, mutable objects.

        if (aso instanceof OptimizedApplicationStateObject)
        {
            OptimizedApplicationStateObject optimized = (OptimizedApplicationStateObject) aso;

            return optimized.isApplicationStateObjectDirty();
        }

        // If not optimized, assume that it is (in fact) dirty.

        return true;
    }
}
