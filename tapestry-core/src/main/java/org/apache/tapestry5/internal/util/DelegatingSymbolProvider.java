// Copyright 2012 The Apache Software Foundation
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

package org.apache.tapestry5.internal.util;

import org.apache.tapestry5.ioc.services.SymbolProvider;

/**
 * Combines two symbol providers.
 *
 * @since 5.4
 */
public class DelegatingSymbolProvider implements SymbolProvider
{
    private final SymbolProvider primary, secondary;

    public DelegatingSymbolProvider(SymbolProvider primary, SymbolProvider secondary)
    {
        this.primary = primary;
        this.secondary = secondary;
    }

    @Override
    public String valueForSymbol(String symbolName)
    {
        String value = primary.valueForSymbol(symbolName);

        if (value == null)
        {
            value = secondary.valueForSymbol(symbolName);
        }

        return value;
    }
}
