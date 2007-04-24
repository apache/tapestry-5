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

package org.apache.tapestry.ioc.internal.services;

import java.util.Map;

import org.apache.tapestry.ioc.ObjectProvider;
import org.apache.tapestry.ioc.ServiceLocator;
import org.apache.tapestry.ioc.internal.util.CollectionFactory;
import org.apache.tapestry.ioc.services.SymbolSource;
import org.apache.tapestry.ioc.services.TypeCoercer;

/**
 * Master implementation of {@link org.apache.tapestry.ioc.ObjectProvider} that delegates out to
 * specific implementations of ObjectProvider. This class enapsulates the logic for extracting a
 * provider prefix, and using it to locate a specific instance, from the service's configuration.
 */
public class MasterObjectProvider implements ObjectProvider
{
    private final Map<String, ObjectProvider> _configuration;

    private final SymbolSource _symbolSource;

    private final TypeCoercer _typeCoercer;

    public MasterObjectProvider(Map<String, ObjectProvider> configuration,
            SymbolSource symbolSource, TypeCoercer typeCoercer)
    {
        _configuration = CollectionFactory.newCaseInsensitiveMap(configuration);

        _symbolSource = symbolSource;
        _typeCoercer = typeCoercer;
    }

    /**
     * The first parameter here isn't just an expression, its the entire object reference, including
     * the prefix (and the colon separator).
     */
    public <T> T provide(String reference, Class<T> objectType, ServiceLocator locator)
    {
        String expanded = _symbolSource.expandSymbols(reference);

        int colonx = expanded.indexOf(':');
        if (colonx < 0) { return _typeCoercer.coerce(expanded, objectType); }

        String prefix = expanded.substring(0, colonx);

        ObjectProvider provider = _configuration.get(prefix);

        if (provider == null)
            throw new RuntimeException(ServiceMessages.unknownObjectProvider(prefix, expanded));

        String expression = expanded.substring(colonx + 1);

        return provider.provide(expression, objectType, locator);
    }

}
