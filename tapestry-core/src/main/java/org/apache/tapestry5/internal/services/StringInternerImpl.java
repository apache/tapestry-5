// Copyright 2009 The Apache Software Foundation
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

import org.apache.tapestry5.ioc.internal.util.CollectionFactory;
import org.apache.tapestry5.services.InvalidationListener;

import java.util.Map;

public class StringInternerImpl implements StringInterner, InvalidationListener
{
    private final Map<String, String> cache = CollectionFactory.newConcurrentMap();

    public void objectWasInvalidated()
    {
        cache.clear();
    }

    public String intern(String string)
    {
        String result = cache.get(string);

        // Not yet in the cache?  Add it.

        if (result == null)
        {
            cache.put(string, string);
            result = string;
        }

        return result;
    }

    public String format(String format, Object... arguments)
    {
        return intern(String.format(format, arguments));
    }
}
