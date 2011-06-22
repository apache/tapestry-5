// Copyright 2010 The Apache Software Foundation
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.apache.tapestry5.internal.services;

import org.apache.tapestry5.ioc.MessageFormatter;
import org.apache.tapestry5.ioc.Messages;

/**
 * Implementation of {@link Messages} that wraps two other Messages instances: a primary and a delegate.
 * The primary handles any keys it contains; method invocations that reference keys not contained by
 * the primary are passed on to the delegate.
 * 
 * @since 5.2.0
 */
public class DelegatingMessagesImpl implements Messages
{
    private final Messages primary, delegate;

    public DelegatingMessagesImpl(Messages primary, Messages delegate)
    {
        this.primary = primary;
        this.delegate = delegate;
    }

    public boolean contains(String key)
    {
        return primary.contains(key) || delegate.contains(key);
    }

    private Messages select(String key)
    {
        return primary.contains(key) ? primary : delegate;
    }

    public String format(String key, Object... args)
    {
        return select(key).format(key, args);
    }

    public String get(String key)
    {
        return select(key).get(key);
    }

    public MessageFormatter getFormatter(String key)
    {
        return select(key).getFormatter(key);
    }
}
