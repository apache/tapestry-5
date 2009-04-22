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

import org.apache.tapestry5.services.ApplicationStateCreator;
import org.apache.tapestry5.services.ApplicationStatePersistenceStrategy;
import org.apache.tapestry5.services.Request;
import org.apache.tapestry5.services.Session;

/**
 * Stores ASOs in the {@link Session}, which will be created as necessary.
 */
public class SessionApplicationStatePersistenceStrategy implements ApplicationStatePersistenceStrategy
{
    static final String PREFIX = "sso:";

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
    public <T> T get(Class<T> ssoClass, ApplicationStateCreator<T> creator)
    {
        Session session = getSession();

        String key = buildKey(ssoClass);

        T sso = (T) session.getAttribute(key);

        if (sso == null)
        {
            sso = creator.create();
            session.setAttribute(key, sso);
        }

        return sso;
    }

    private <T> String buildKey(Class<T> ssoClass)
    {
        return PREFIX + ssoClass.getName();
    }

    public <T> void set(Class<T> ssoClass, T sso)
    {
        String key = buildKey(ssoClass);

        getSession().setAttribute(key, sso);
    }

    public <T> boolean exists(Class<T> ssoClass)
    {
        String key = buildKey(ssoClass);

        Session session = request.getSession(false);

        return session != null && session.getAttribute(key) != null;
    }
}
