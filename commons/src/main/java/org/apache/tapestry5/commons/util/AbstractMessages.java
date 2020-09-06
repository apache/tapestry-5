// Copyright 2006, 2009, 2011 The Apache Software Foundation
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

package org.apache.tapestry5.commons.util;

import java.util.Locale;
import java.util.Map;

import org.apache.tapestry5.commons.MessageFormatter;
import org.apache.tapestry5.commons.Messages;
import org.apache.tapestry5.commons.internal.util.MessageFormatterImpl;

/**
 * Abstract implementation of {@link Messages} that doesn't know where values come from (that information is supplied in
 * a subclass, via the {@link #valueForKey(String)} method).
 */
public abstract class AbstractMessages implements Messages
{
    /**
     * String key to MF instance.
     */
    private final Map<String, MessageFormatter> cache = CollectionFactory.newConcurrentMap();

    private final Locale locale;

    protected AbstractMessages(Locale locale)
    {
        this.locale = locale;
    }

    /**
     * Invoked to provide the value for a particular key. This may be invoked multiple times even for the same key. The
     * implementation should <em>ignore the case of the key</em>.
     *
     * @param key the key to obtain a value for (case insensitive)
     * @return the value for the key, or null if this instance can not provide the value
     */
    protected abstract String valueForKey(String key);


    @Override
    public boolean contains(String key)
    {
        return valueForKey(key) != null;
    }

    @Override
    public String get(String key)
    {
        if (contains(key)) return valueForKey(key);

        return String.format("[[missing key: %s]]", key);
    }

    @Override
    public MessageFormatter getFormatter(String key)
    {
        MessageFormatter result = cache.get(key);

        if (result == null)
        {
            result = buildMessageFormatter(key);
            cache.put(key, result);
        }

        return result;
    }

    private MessageFormatter buildMessageFormatter(String key)
    {
        String format = get(key);

        return new MessageFormatterImpl(format, locale);
    }

    @Override
    public String format(String key, Object... args)
    {
        return getFormatter(key).format(args);
    }

}
