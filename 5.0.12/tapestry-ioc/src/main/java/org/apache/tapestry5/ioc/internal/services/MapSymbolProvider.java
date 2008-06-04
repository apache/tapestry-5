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

package org.apache.tapestry5.ioc.internal.services;

import org.apache.tapestry5.ioc.services.SymbolProvider;

import java.util.Map;

/**
 * Provides symbol values from a Map of symbol names and symbol values (typically provided by a Tapestry IOC service
 * configuration).
 */
public class MapSymbolProvider implements SymbolProvider
{
    private final Map<String, String> configuration;

    public MapSymbolProvider(final Map<String, String> configuration)
    {
        this.configuration = configuration;
    }

    public String valueForSymbol(String symbolName)
    {
        return configuration.get(symbolName);
    }

}
