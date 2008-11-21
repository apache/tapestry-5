// Copyright 2006, 2007, 2008 The Apache Software Foundation
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

package org.apache.tapestry5.ioc.services;

import org.apache.tapestry5.ioc.annotations.UsesOrderedConfiguration;
import org.apache.tapestry5.ioc.annotations.Value;

/**
 * Used to manage <em>symbols</em>, configuration properties whose value is evaluated at runtime. Symbols use the Ant
 * syntax: <code>${foo.bar.baz}</code> where <code>foo.bar.baz</code> is the name of the symbol. The symbol may appear
 * inside annotation, such as {@link Value}.
 * <p/>
 * The SymbolSource service configuration is an ordered list of {@link org.apache.tapestry5.ioc.services.SymbolProvider}s.
 * Two key SymbolProvider services are FactoryDefaults and ApplicationDefaults.
 */
@UsesOrderedConfiguration(SymbolProvider.class)
public interface SymbolSource
{
    /**
     * Expands the value for a particular symbol. This may involve recursive expansion, if the immediate value for the
     * symbol itself contains symbols.
     *
     * @param symbolName
     * @return the expanded string
     * @throws RuntimeException if the symbol name can not be expanded (no {@link SymbolProvider} can provide its
     *                          value), or if an expansion is directly or indirectly recursive
     */
    String valueForSymbol(String symbolName);

    /**
     * Given an input string that <em>may</em> contain symbols, returns the string with any and all symbols fully
     * expanded.
     *
     * @param input
     * @return expanded input
     */
    String expandSymbols(String input);
}
