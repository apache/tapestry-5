// Copyright 2008, 2009 The Apache Software Foundation
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

package org.apache.tapestry5.dom;

import org.apache.tapestry5.ioc.internal.util.CollectionFactory;

import java.util.Collections;
import java.util.Map;

/**
 * Used by {@link org.apache.tapestry5.dom.Element} to construct namespace URI to prefix maps.
 *
 * @since 5.0.19
 */
class MapHolder
{
    private static final Map<String, String> EMPTY_MAP = Collections.emptyMap();

    private final Map<String, String> startingMap;

    private Map<String, String> localMap;

    MapHolder()
    {
        this(null);
    }

    MapHolder(Map<String, String> startingMap)
    {
        this.startingMap = startingMap == null ? EMPTY_MAP : startingMap;
    }

    void put(String key, String value)
    {
        getMutable().put(key, value);
    }

    Map<String, String> getMutable()
    {
        if (localMap == null)
            localMap = CollectionFactory.newMap(startingMap);

        return localMap;
    }

    void putAll(Map<String, String> map)
    {
        if (map == null || map.isEmpty()) return;

        getMutable().putAll(map);
    }

    Map<String, String> getResult()
    {
        return localMap != null ? localMap : startingMap;
    }
}
