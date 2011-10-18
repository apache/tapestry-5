//  Copyright 2011 The Apache Software Foundation
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

package org.apache.tapestry5.internal.services;

import org.apache.tapestry5.SymbolConstants;
import org.apache.tapestry5.ioc.annotations.Symbol;
import org.apache.tapestry5.services.Session;
import org.apache.tapestry5.services.SessionPersistedObjectAnalyzer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

public class TapestrySessionFactoryImpl implements TapestrySessionFactory
{
    private boolean clustered;
    private final SessionPersistedObjectAnalyzer analyzer;
    private final HttpServletRequest request;

    public TapestrySessionFactoryImpl(
            @Symbol(SymbolConstants.CLUSTERED_SESSIONS)
            boolean clustered,
            SessionPersistedObjectAnalyzer analyzer,
            HttpServletRequest request)
    {
        this.clustered = clustered;
        this.analyzer = analyzer;
        this.request = request;
    }

    public Session getSession(boolean create)
    {
        final HttpSession httpSession = request.getSession(create);

        if (httpSession == null)
        {
            return null;
        }

        if (clustered)
        {
            return new ClusteredSessionImpl(request, httpSession, analyzer);
        }

        return new SessionImpl(request, httpSession);
    }
}
