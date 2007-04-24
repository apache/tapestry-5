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

package org.apache.tapestry.ioc.internal.services;

import java.util.Collections;
import java.util.Map;

import org.apache.tapestry.ioc.ObjectProvider;
import org.apache.tapestry.ioc.ServiceLocator;
import org.apache.tapestry.ioc.internal.IOCInternalTestCase;
import org.apache.tapestry.ioc.internal.util.CollectionFactory;
import org.apache.tapestry.ioc.services.SymbolSource;
import org.apache.tapestry.ioc.services.TypeCoercer;
import org.testng.annotations.Test;

public class MasterObjectProviderTest extends IOCInternalTestCase
{
    private Map<String, ObjectProvider> newMap(String prefix, ObjectProvider provider)
    {
        return CollectionFactory.newCaseInsensitiveMap(Collections.singletonMap(prefix, provider));
    }

    @Test
    public void successful_lookup()
    {
        ObjectProvider provider = newObjectProvider();
        ServiceLocator locator = newServiceLocator();
        Runnable r = newRunnable();
        SymbolSource source = newSymbolSource();

        train_expandSymbols(source, "prefix:expression");
        train_provide(provider, "expression", Runnable.class, locator, r);

        replay();

        ObjectProvider master = new MasterObjectProvider(newMap("prefix", provider), source, null);

        Runnable actual = master.provide("prefix:expression", Runnable.class, locator);

        assertSame(actual, r);

        verify();
    }

    @Test
    public void successful_lookup_case_insensitive()
    {
        ObjectProvider provider = newObjectProvider();
        ServiceLocator locator = newServiceLocator();
        Runnable r = newRunnable();
        SymbolSource source = newSymbolSource();

        train_expandSymbols(source, "PREFIX:expression");
        train_provide(provider, "expression", Runnable.class, locator, r);

        replay();

        ObjectProvider master = new MasterObjectProvider(newMap("prefix", provider), source, null);

        Runnable actual = master.provide("PREFIX:expression", Runnable.class, locator);

        assertSame(actual, r);

        verify();
    }

    @Test
    public void symbols_are_expanded()
    {
        ObjectProvider provider = newObjectProvider();
        ServiceLocator locator = newServiceLocator();
        Runnable r = newRunnable();
        SymbolSource source = newSymbolSource();

        train_expandSymbols(source, "${symbol}", "prefix:expression");
        train_provide(provider, "expression", Runnable.class, locator, r);

        replay();

        ObjectProvider master = new MasterObjectProvider(newMap("prefix", provider), source, null);

        Runnable actual = master.provide("${symbol}", Runnable.class, locator);

        assertSame(actual, r);

        verify();
    }

    @Test
    public void unknown_object_provider_prefix()
    {
        ServiceLocator locator = newServiceLocator();
        SymbolSource source = newSymbolSource();

        train_expandSymbols(source, "prefix:expression");

        replay();

        Map<String, ObjectProvider> configuration = CollectionFactory.newMap();

        ObjectProvider master = new MasterObjectProvider(configuration, source, null);

        try
        {
            master.provide("prefix:expression", Runnable.class, locator);
            unreachable();
        }
        catch (RuntimeException ex)
        {
            assertEquals(
                    ex.getMessage(),
                    "Object provider 'prefix' does not exist (in object reference 'prefix:expression').");
        }

        verify();
    }

    @Test
    public void no_prefix_in_object_reference()
    {
        ServiceLocator locator = newServiceLocator();
        SymbolSource source = newSymbolSource();
        TypeCoercer coercer = newTypeCoercer();

        train_expandSymbols(source, "${value}", "55");
        train_coerce(coercer, "55", Integer.class, 55);

        replay();

        Map<String, ObjectProvider> configuration = CollectionFactory.newMap();

        ObjectProvider master = new MasterObjectProvider(configuration, source, coercer);

        assertEquals(master.provide("${value}", Integer.class, locator), new Integer(55));

        verify();
    }

}
