// Copyright 2006 The Apache Software Foundation
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

package org.apache.tapestry.ioc.util;

import static org.apache.tapestry.ioc.internal.util.CollectionFactory.newThreadSafeMap;

import java.util.Map;

import org.apache.tapestry.ioc.MessageFormatter;
import org.apache.tapestry.ioc.Messages;
import org.apache.tapestry.ioc.internal.util.MessageFormatterImpl;

/**
 * Abstract implementation of {@link Messages} that doesn't know where values come from (that
 * information is supplied in a subclass, via the {@link #valueForKey(String)} method).
 */
public abstract class AbstractMessages implements Messages
{
    /** String key to MF instance. */
    private Map<String, MessageFormatter> _cache = newThreadSafeMap();

    /**
     * Invoked to provide the value for a particular key. This may be invoked multiple times even
     * for the same key.
     * 
     * @param key
     *            the key to obtain a value for
     * @return the value for the key, or null if this instance can not provide the value
     */
    protected abstract String valueForKey(String key);

    public boolean contains(String key)
    {
        return valueForKey(key) != null;
    }

    public String get(String key)
    {
        if (contains(key)) return valueForKey(key);

        return String.format("[[missing key: %s]]", key);
    }

    public MessageFormatter getFormatter(String key)
    {
        MessageFormatter result = _cache.get(key);

        if (result == null)
        {
            result = buildMessageFormatter(key);
            _cache.put(key, result);
        }

        return result;
    }

    private MessageFormatter buildMessageFormatter(String key)
    {
        String format = get(key);

        return new MessageFormatterImpl(format);
    }

    public String format(String key, Object... args)
    {
        return getFormatter(key).format(args);
    }

}
