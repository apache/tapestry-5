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
import org.apache.tapestry5.ioc.test.TestBase;
import org.testng.annotations.Test;

import javax.servlet.ServletContext;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;

public class ServletContextSymbolProviderTest extends TestBase
{
    @Test
    public void access_of_keys_is_case_insensitive()
    {
        ServletContext context = newMock(ServletContext.class);

        String key1 = "fred";
        String value1 = "Fred Flintstone";
        String key2 = "barney";
        String value2 = "Barney Rubble";

        expect(context.getInitParameterNames()).andReturn(toEnumeration(key1, key2));

        expect(context.getInitParameter(key1)).andReturn(value1);
        expect(context.getInitParameter(key2)).andReturn(value2);

        replay();

        SymbolProvider p = new ServletContextSymbolProvider(context);

        assertEquals(p.valueForSymbol(key1), value1);
        assertEquals(p.valueForSymbol(key2), value2);

        // Not in config is null
        assertNull(p.valueForSymbol("wilma"));

        // Check for case insensitivity
        assertEquals(p.valueForSymbol("FRED"), value1);

        verify();
    }

    protected final <T> Enumeration<T> toEnumeration(T... values)
    {
        return Collections.enumeration(Arrays.asList(values));
    }

}
