// Copyright 2007 The Apache Software Foundation
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

package org.apache.tapestry5.internal;

import org.apache.tapestry5.ioc.services.SymbolProvider;

/**
 * Implementation of {@link SymbolProvider} that only supports a single key/value pair.
 */
public class SingleKeySymbolProvider implements SymbolProvider
{
    private final String symbolName;

    private final String value;

    public SingleKeySymbolProvider(final String symbolName, final String value)
    {
        this.symbolName = symbolName;
        this.value = value;
    }

    public String valueForSymbol(String symbolName)
    {
        if (this.symbolName.equalsIgnoreCase(symbolName)) return value;

        return null;
    }

}
