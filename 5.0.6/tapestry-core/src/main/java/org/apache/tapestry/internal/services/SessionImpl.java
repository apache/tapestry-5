// Copyright 2006, 2007 The Apache Software Foundation
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

import static org.apache.tapestry.ioc.internal.util.CollectionFactory.newList;

import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import javax.servlet.http.HttpSession;

import org.apache.tapestry.ioc.internal.util.InternalUtils;
import org.apache.tapestry.services.Session;

/**
 * A thin wrapper around {@link HttpSession}.
 */
public class SessionImpl implements Session
{
    private final HttpSession _session;

    public SessionImpl(HttpSession session)
    {
        _session = session;
    }

    public Object getAttribute(String name)
    {
        return _session.getAttribute(name);
    }

    public List<String> getAttributeNames()
    {
        return InternalUtils.toList(_session.getAttributeNames());
    }

    public void setAttribute(String name, Object value)
    {
        _session.setAttribute(name, value);
    }

    public List<String> getAttributeNames(String prefix)
    {
        List<String> result = newList();

        Enumeration e = _session.getAttributeNames();
        while (e.hasMoreElements())
        {
            String name = (String) e.nextElement();

            if (name.startsWith(prefix)) result.add(name);
        }

        Collections.sort(result);

        return result;
    }

    public int getMaxInactiveInterval()
    {
        return _session.getMaxInactiveInterval();
    }

    public void invalidate()
    {
        _session.invalidate();
    }

    public void setMaxInactiveInterval(int seconds)
    {
        _session.setMaxInactiveInterval(seconds);
    }

}
