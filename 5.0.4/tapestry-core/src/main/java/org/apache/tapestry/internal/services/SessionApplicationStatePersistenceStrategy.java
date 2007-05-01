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

import org.apache.tapestry.services.ApplicationStateCreator;
import org.apache.tapestry.services.ApplicationStatePersistenceStrategy;
import org.apache.tapestry.services.Session;

/**
 * Stores ASOs in the {@link Session}, which will be created as necessary.
 * <p>
 * TODO: Re-storing the object back into the session at the end of the request. That's going to
 * require some kind of end-of-request notification.
 */
public class SessionApplicationStatePersistenceStrategy implements
        ApplicationStatePersistenceStrategy
{
    static final String PREFIX = "aso:";

    private final SessionHolder _sessionHolder;

    public SessionApplicationStatePersistenceStrategy(SessionHolder sessionHolder)
    {
        _sessionHolder = sessionHolder;
    }

    private Session getSession()
    {
        return _sessionHolder.getSession(true);
    }

    @SuppressWarnings("unchecked")
    public <T> T get(Class<T> asoClass, ApplicationStateCreator<T> creator)
    {
        Session session = getSession();

        String key = buildKey(asoClass);

        T aso = (T) session.getAttribute(key);

        if (aso == null)
        {
            aso = creator.create();
            session.setAttribute(key, aso);
        }

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
    }

    public <T> boolean exists(Class<T> asoClass)
    {
        String key = buildKey(asoClass);

        Session session = _sessionHolder.getSession(false);

        return session != null && session.getAttribute(key) != null;
    }

}
