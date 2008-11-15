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

package org.apache.tapestry5.internal.structure;

import org.apache.tapestry5.internal.services.ComponentClassCache;
import org.apache.tapestry5.internal.services.ComponentMessagesSource;
import org.apache.tapestry5.ioc.LoggerSource;
import org.apache.tapestry5.ioc.internal.util.CollectionFactory;
import org.apache.tapestry5.ioc.internal.util.Defense;
import org.apache.tapestry5.ioc.services.TypeCoercer;
import org.apache.tapestry5.services.ContextValueEncoder;

import java.util.Locale;
import java.util.Map;

public class PageResourcesSourceImpl implements PageResourcesSource
{
    private final Map<Locale, PageResources> cache = CollectionFactory.newConcurrentMap();

    private final ComponentMessagesSource componentMessagesSource;

    private final TypeCoercer typeCoercer;

    private final ComponentClassCache componentClassCache;

    private final ContextValueEncoder contextValueEncoder;

    private final LoggerSource loggerSource;

    public PageResourcesSourceImpl(ComponentMessagesSource componentMessagesSource, TypeCoercer typeCoercer,
                                   ComponentClassCache componentClassCache, ContextValueEncoder contextValueEncoder,
                                   LoggerSource loggerSource)
    {
        this.componentMessagesSource = componentMessagesSource;
        this.typeCoercer = typeCoercer;
        this.componentClassCache = componentClassCache;
        this.contextValueEncoder = contextValueEncoder;
        this.loggerSource = loggerSource;
    }

    public PageResources get(Locale locale)
    {
        Defense.notNull(locale, "locale");

        PageResources result = cache.get(locale);

        if (result == null)
        {
            result = new PageResourcesImpl(locale, componentMessagesSource, typeCoercer, componentClassCache,
                                           contextValueEncoder, loggerSource);

            // Small race condition here, where we may create two instances of PRI for the same locale,
            // but that's not worth worrying about.

            cache.put(locale, result);
        }

        return result;
    }
}
