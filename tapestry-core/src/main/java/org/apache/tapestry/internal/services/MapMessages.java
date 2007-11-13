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

package org.apache.tapestry.internal.services;

/**
 * An implementation  of {@link Messages} that is based on a map.
 *
 */

import org.apache.tapestry.ioc.Messages;
import org.apache.tapestry.ioc.util.AbstractMessages;

import java.util.Locale;
import java.util.Map;

/**
 * Implementation of {@link Messages} based on a simple Map (of string keys and values).
 */
public class MapMessages extends AbstractMessages
{
    private final Map<String, String> _properties;


    /**
     * A new instance <strong>retaining</strong> (not copying) the provided map.
     */
    public MapMessages(Locale locale, Map<String, String> properties)
    {
        super(locale);
        _properties = properties;
    }


    @Override
    protected String valueForKey(String key)
    {
        return _properties.get(key);
    }
}
