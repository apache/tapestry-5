// Copyright 2006, 2007, 2008, 2024 The Apache Software Foundation
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

package org.apache.tapestry5.internal.test;

import org.apache.tapestry5.commons.util.CollectionFactory;
import org.apache.tapestry5.http.services.Session;
import org.apache.tapestry5.ioc.internal.util.InternalUtils;

import static org.apache.tapestry5.commons.util.CollectionFactory.newList;

import java.util.List;
import java.util.Map;

public class PageTesterSession implements Session
{
    private final Map<String, Object> attributes = CollectionFactory.newMap();

    @Override
    public List<String> getAttributeNames()
    {
        return InternalUtils.sortedKeys(attributes);
    }

    @Override
    public List<String> getAttributeNames(Session.LockMode lockMode)
    {
        return getAttributeNames();
    }

    @Override
    public List<String> getAttributeNames(String prefix)
    {
        List<String> result = newList();

        for (String name : getAttributeNames())
            if (name.startsWith(prefix)) result.add(name);

        return result;
    }

    @Override
    public List<String> getAttributeNames(String prefix, LockMode lockMode)
    {
        return getAttributeNames(prefix);
    }

    @Override
    public Object getAttribute(String name)
    {
        return attributes.get(name);
    }

    @Override
    public Object getAttribute(String name, LockMode lockMode)
    {
        return getAttribute(name);
    }

    @Override
    public void setAttribute(String name, Object value)
    {
        if (value == null)
        {
            attributes.remove(name);
        }
        else
        {
            attributes.put(name, value);
        }
    }

    @Override
    public boolean containsAttribute(String name)
    {
        return attributes.containsKey(name);
    }

    @Override
    public boolean containsAttribute(String name, Session.LockMode lockMode)
    {
        return containsAttribute(name);
    }

    private void nyi(String name)
    {
        throw new IllegalStateException(String.format("%s.%s() is not yet implemented.", getClass()
                .getName(), name));
    }

    @Override
    public int getMaxInactiveInterval()
    {
        nyi("getMaxInativeInterval");

        return 0;
    }

    @Override
    public void invalidate()
    {
        nyi("invalidate");
    }

    @Override
    public boolean isInvalidated()
    {
        return false;
    }

    @Override
    public void restoreDirtyObjects()
    {
    }

    @Override
    public void setMaxInactiveInterval(int seconds)
    {
        nyi("setMaxInactiveInterval");
    }
}
