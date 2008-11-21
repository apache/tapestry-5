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

import org.apache.tapestry5.ioc.internal.IOCInternalTestCase;
import org.apache.tapestry5.ioc.services.SymbolProvider;
import org.apache.tapestry5.ioc.services.SymbolSource;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class SymbolSourceImplTest extends IOCInternalTestCase
{
    @Test
    public void expand_symbols_no_symbols_present()
    {
        String input = "A jolly good time.";

        List<SymbolProvider> providers = Collections.emptyList();

        SymbolSource source = new SymbolSourceImpl(providers);

        // Not just equal, but the same.

        assertSame(source.expandSymbols(input), input);
    }

    @Test
    public void simple_expansions()
    {
        SymbolProvider provider = mockSymbolProvider();

        List<SymbolProvider> providers = Arrays.asList(provider);

        train_valueForSymbol(provider, "barney", "Barney");
        train_valueForSymbol(provider, "dino", "Dino");

        replay();

        SymbolSource source = new SymbolSourceImpl(providers);

        assertEquals(
                source.expandSymbols("Fred's friends are ${barney} and ${dino}."),
                "Fred's friends are Barney and Dino.");

        verify();
    }

    @Test
    public void undefined_symbol()
    {

        SymbolProvider provider = mockSymbolProvider();

        List<SymbolProvider> providers = Arrays.asList(provider);

        train_valueForSymbol(provider, "barney", null);

        replay();

        SymbolSource source = new SymbolSourceImpl(providers);

        try
        {
            source.valueForSymbol("barney");
            unreachable();
        }
        catch (RuntimeException ex)
        {
            assertEquals(ex.getMessage(), "Symbol 'barney' is not defined.");
        }

        verify();
    }

    @Test
    public void missing_brace()
    {
        SymbolProvider provider = mockSymbolProvider();

        List<SymbolProvider> providers = Arrays.asList(provider);

        replay();

        SymbolSource source = new SymbolSourceImpl(providers);

        try
        {
            source.expandSymbols("Unmatched ${this");
            unreachable();
        }
        catch (RuntimeException ex)
        {
            assertEquals(
                    ex.getMessage(),
                    "Input string 'Unmatched ${this' is missing a symbol closing brace.");
        }

        verify();
    }

    @Test
    public void indirect_expansions()
    {
        SymbolProvider provider = mockSymbolProvider();

        List<SymbolProvider> providers = Arrays.asList(provider);

        train_valueForSymbol(provider, "fred.friends", "${barney} and ${dino}");
        train_valueForSymbol(provider, "barney", "Barney");
        train_valueForSymbol(provider, "dino", "Dino");

        replay();

        SymbolSource source = new SymbolSourceImpl(providers);

        assertEquals(
                source.expandSymbols("Fred's friends are ${fred.friends}."),
                "Fred's friends are Barney and Dino.");

        verify();
    }

    @Test
    public void undefined_symbol_in_path()
    {

        SymbolProvider provider = mockSymbolProvider();

        List<SymbolProvider> providers = Arrays.asList(provider);

        train_valueForSymbol(provider, "barney", "Barney (whose friends are ${barney.friends})");
        train_valueForSymbol(provider, "barney.friends", "${fred} and ${betty}");
        train_valueForSymbol(provider, "fred", null);

        replay();

        SymbolSource source = new SymbolSourceImpl(providers);

        try
        {
            source.valueForSymbol("barney");
            unreachable();
        }
        catch (RuntimeException ex)
        {
            assertEquals(
                    ex.getMessage(),
                    "Symbol 'fred' is not defined (in barney --> barney.friends --> fred). ");
        }

        verify();
    }

    @Test
    public void missing_brace_in_path()
    {

        SymbolProvider provider = mockSymbolProvider();

        List<SymbolProvider> providers = Arrays.asList(provider);

        train_valueForSymbol(provider, "barney", "Barney (whose friends are ${barney.friends})");
        train_valueForSymbol(provider, "barney.friends", "${fred} and ${betty");
        train_valueForSymbol(provider, "fred", "Fred");

        replay();

        SymbolSource source = new SymbolSourceImpl(providers);

        try
        {
            source.valueForSymbol("barney");
            unreachable();
        }
        catch (RuntimeException ex)
        {
            assertEquals(
                    ex.getMessage(),
                    "Input string '${fred} and ${betty' is missing a symbol closing brace (in barney --> barney.friends).");
        }

        verify();
    }

    @Test
    public void providers_searched_in_order()
    {
        SymbolProvider provider1 = mockSymbolProvider();
        SymbolProvider provider2 = mockSymbolProvider();

        List<SymbolProvider> providers = Arrays.asList(provider1, provider2);

        train_valueForSymbol(provider1, "fred.friends", "${barney} and ${dino}");
        train_valueForSymbol(provider1, "barney", null);
        train_valueForSymbol(provider2, "barney", "Barney");
        train_valueForSymbol(provider1, "dino", null);
        train_valueForSymbol(provider2, "dino", "Dino");

        replay();

        SymbolSource source = new SymbolSourceImpl(providers);

        assertEquals(
                source.expandSymbols("Fred's friends are ${fred.friends}."),
                "Fred's friends are Barney and Dino.");

        verify();
    }

    @Test
    public void symbols_are_cached()
    {
        SymbolProvider provider = mockSymbolProvider();

        List<SymbolProvider> providers = Arrays.asList(provider);

        train_valueForSymbol(provider, "fred", "Fred's friends are ${barney} and ${dino}.");
        train_valueForSymbol(provider, "barney", "Barney");
        train_valueForSymbol(provider, "dino", "Dino");

        replay();

        SymbolSource source = new SymbolSourceImpl(providers);

        assertEquals(source.valueForSymbol("fred"), "Fred's friends are Barney and Dino.");

        verify();

        replay();

        // This time, comes out of the cache.

        assertEquals(source.valueForSymbol("fred"), "Fred's friends are Barney and Dino.");

        verify();
    }

    @Test
    public void recursive_symbols_fail()
    {
        SymbolProvider provider = mockSymbolProvider();

        List<SymbolProvider> providers = Arrays.asList(provider);

        train_valueForSymbol(provider, "fred", "Fred (whose friends are ${fred.friends})");
        train_valueForSymbol(provider, "fred.friends", "${barney} and ${dino}");
        train_valueForSymbol(provider, "barney", "Barney (whose friends are ${barney.friends})");
        train_valueForSymbol(provider, "barney.friends", "${fred} and ${betty}");

        replay();

        SymbolSource source = new SymbolSourceImpl(providers);

        try
        {
            source.valueForSymbol("fred");
            unreachable();
        }
        catch (RuntimeException ex)
        {
            assertEquals(
                    ex.getMessage(),
                    "Symbol 'fred' is defined in terms of itself (fred --> fred.friends --> barney --> barney.friends --> fred).");
        }

        verify();
    }

    @Test
    public void integration_test()
    {
        SymbolSource source = getService(SymbolSource.class);

        // SystemPropertiesSymbolProvider is available by default

        String userName = System.getProperty("user.name");

        assertEquals(source.valueForSymbol("user.name"), userName);
    }

    protected final void train_valueForSymbol(SymbolProvider provider, String symbolName,
                                              String value)
    {
        expect(provider.valueForSymbol(symbolName)).andReturn(value);
    }

    protected final SymbolProvider mockSymbolProvider()
    {
        return newMock(SymbolProvider.class);
    }

}
