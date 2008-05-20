// Copyright 2006, 2007 The Apache Software Foundation
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

import org.apache.tapestry5.ioc.internal.util.CollectionFactory;
import static org.apache.tapestry5.ioc.internal.util.CollectionFactory.newLinkedList;
import org.apache.tapestry5.ioc.services.SymbolProvider;
import org.apache.tapestry5.ioc.services.SymbolSource;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class SymbolSourceImpl implements SymbolSource
{
    private final List<SymbolProvider> providers;

    /**
     * Cache of symbol name to fully expanded symbol value.
     */
    private final Map<String, String> cache = CollectionFactory.newConcurrentMap();

    /**
     * Contains execution data needed when performing an expansion (largely, to check for endless recursion).
     */
    private class SymbolExpansion
    {
        private final LinkedList<String> expandingSymbols = newLinkedList();

        String expandSymbols(String input)
        {
            StringBuilder builder = null;

            int startx = 0;

            while (true)
            {
                int symbolx = input.indexOf("${", startx);

                // Special case: if the string contains no symbols then return it as is.

                if (startx == 0 && symbolx < 0) return input;

                // The string has at least one symbol, so its OK to create the StringBuilder

                if (builder == null) builder = new StringBuilder();

                // No more symbols found, so add in the rest of the string.

                if (symbolx < 0)
                {
                    builder.append(input.substring(startx));
                    break;
                }

                builder.append(input.substring(startx, symbolx));

                int endx = input.indexOf("}", symbolx);

                if (endx < 0)
                {
                    String message = expandingSymbols.isEmpty() ? ServiceMessages
                            .missingSymbolCloseBrace(input) : ServiceMessages
                            .missingSymbolCloseBraceInPath(input, path());

                    throw new RuntimeException(message);
                }

                String symbolName = input.substring(symbolx + 2, endx);

                builder.append(valueForSymbol(symbolName));

                // Restart the search after the '}'

                startx = endx + 1;
            }

            return builder.toString();
        }

        String valueForSymbol(String symbolName)
        {
            String value = cache.get(symbolName);

            if (value == null)
            {
                value = expandSymbol(symbolName);

                cache.put(symbolName, value);
            }

            return value;
        }

        String expandSymbol(String symbolName)
        {
            if (expandingSymbols.contains(symbolName))
            {
                expandingSymbols.add(symbolName);
                throw new RuntimeException(ServiceMessages.recursiveSymbol(
                        symbolName,
                        pathFrom(symbolName)));
            }

            expandingSymbols.addLast(symbolName);

            String value = null;

            for (SymbolProvider provider : providers)
            {
                value = provider.valueForSymbol(symbolName);

                if (value != null) break;
            }

            if (value == null)
            {

                String message = expandingSymbols.size() == 1 ? ServiceMessages
                        .symbolUndefined(symbolName) : ServiceMessages.symbolUndefinedInPath(
                        symbolName,
                        path());

                throw new RuntimeException(message);
            }

            // The value may have symbols that need expansion.

            String result = expandSymbols(value);

            // And we're done expanding this symbol

            expandingSymbols.removeLast();

            return result;

        }

        String path()
        {
            StringBuilder builder = new StringBuilder();

            boolean first = true;

            for (String symbolName : expandingSymbols)
            {
                if (!first) builder.append(" --> ");

                builder.append(symbolName);

                first = false;
            }

            return builder.toString();
        }

        String pathFrom(String startSymbolName)
        {
            StringBuilder builder = new StringBuilder();

            boolean first = true;
            boolean match = false;

            for (String symbolName : expandingSymbols)
            {
                if (!match)
                {
                    if (symbolName.equals(startSymbolName))
                        match = true;
                    else
                        continue;
                }

                if (!first) builder.append(" --> ");

                builder.append(symbolName);

                first = false;
            }

            return builder.toString();
        }
    }

    public SymbolSourceImpl(final List<SymbolProvider> providers)
    {
        this.providers = providers;
    }

    public String expandSymbols(String input)
    {
        return new SymbolExpansion().expandSymbols(input);
    }

    public String valueForSymbol(String symbolName)
    {
        String value = cache.get(symbolName);

        // If already in the cache, then return it. Otherwise, let the SE find the value and
        // update the cache.

        return value != null ? value : new SymbolExpansion().valueForSymbol(symbolName);
    }

}
