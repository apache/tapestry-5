// Copyright 2011 The Apache Software Foundation
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

package org.apache.tapestry5.ioc.internal.services;

import org.apache.tapestry5.ioc.internal.util.CollectionFactory;
import org.apache.tapestry5.ioc.services.SymbolProvider;

import java.util.Map;

/**
 * Provides <em>case insensitive</em> access to  environment variables. Environment variable symbols
 * are prefixed with "env.".
 *
 * @since 5.3
 */
public class SystemEnvSymbolProvider implements SymbolProvider
{
    private final Map<String, String> symbols = CollectionFactory.newCaseInsensitiveMap();

    public synchronized String valueForSymbol(String symbolName)
    {
        if (symbols.isEmpty())
        {
            Map<String, String> env = System.getenv();

            for (String key : env.keySet())
            {
                symbols.put("env." + key, env.get(key));
            }
        }


        return symbols.get(symbolName);
    }
}
