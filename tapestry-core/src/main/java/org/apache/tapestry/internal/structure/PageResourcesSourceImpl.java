// Copyright 2008 The Apache Software Foundation
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

package org.apache.tapestry.internal.structure;

import org.apache.tapestry.internal.services.ComponentClassCache;
import org.apache.tapestry.ioc.internal.util.CollectionFactory;
import org.apache.tapestry.ioc.internal.util.Defense;
import org.apache.tapestry.ioc.services.TypeCoercer;
import org.apache.tapestry.services.ComponentMessagesSource;

import java.util.Locale;
import java.util.Map;

public class PageResourcesSourceImpl implements PageResourcesSource
{
    private final Map<Locale, PageResources> _cache = CollectionFactory.newConcurrentMap();

    private final ComponentMessagesSource _componentMessagesSource;

    private final TypeCoercer _typeCoercer;

    private final ComponentClassCache _componentClassCache;

    public PageResourcesSourceImpl(ComponentMessagesSource componentMessagesSource, TypeCoercer typeCoercer,
                                   ComponentClassCache componentClassCache)
    {
        _componentMessagesSource = componentMessagesSource;
        _typeCoercer = typeCoercer;
        _componentClassCache = componentClassCache;
    }

    public PageResources get(Locale locale)
    {
        Defense.notNull(locale, "locale");

        PageResources result = _cache.get(locale);

        if (result == null)
        {
            result = new PageResourcesImpl(locale, _componentMessagesSource, _typeCoercer, _componentClassCache);

            // Small race condition here, where we may create two instances of PRI for the same locale,
            // but that's not worth worrying about.

            _cache.put(locale, result);
        }

        return result;
    }
}
