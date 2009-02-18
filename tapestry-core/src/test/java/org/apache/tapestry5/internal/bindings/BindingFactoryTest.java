// Copyright 2006, 2007, 2008, 2009 The Apache Software Foundation
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

package org.apache.tapestry5.internal.bindings;

import org.apache.tapestry5.Binding;
import org.apache.tapestry5.ComponentResources;
import org.apache.tapestry5.FieldTranslator;
import org.apache.tapestry5.internal.services.StringInternerImpl;
import org.apache.tapestry5.internal.test.InternalBaseTestCase;
import org.apache.tapestry5.ioc.Location;
import org.apache.tapestry5.ioc.internal.util.InternalUtils;
import org.apache.tapestry5.ioc.internal.util.TapestryException;
import org.apache.tapestry5.services.BindingFactory;
import org.apache.tapestry5.services.FieldTranslatorSource;
import org.testng.annotations.Test;

/**
 * Tests for several of the simpler binding factories.
 */
public class BindingFactoryTest extends InternalBaseTestCase
{

    @Test
    public void literal_binding()
    {
        ComponentResources res = mockInternalComponentResources();
        Location l = mockLocation();

        replay();

        BindingFactory factory = new LiteralBindingFactory();

        Binding b = factory.newBinding("test binding", res, null, "Tapestry5", l);

        assertSame(InternalUtils.locationOf(b), l);

        assertEquals(b.get(), "Tapestry5");
        assertTrue(b.isInvariant());
        assertSame(b.getBindingType(), String.class);

        try
        {
            b.set(null);
            unreachable();
        }
        catch (TapestryException ex)
        {
            assertSame(ex.getLocation(), l);
        }

        verify();
    }

    @Test
    public void translate_binding()
    {
        FieldTranslator translator = mockFieldTranslator();
        FieldTranslatorSource source = newMock(FieldTranslatorSource.class);
        ComponentResources resources = mockComponentResources();
        Location l = mockLocation();

        String description = "foo bar";
        String expression = "mock";

        expect(source.createTranslator(resources, expression)).andReturn(translator);

        replay();

        BindingFactory factory = new TranslateBindingFactory(source, new StringInternerImpl());

        Binding binding = factory.newBinding(description, resources, resources, expression, l);

        assertSame(binding.get(), translator);

        assertSame(InternalUtils.locationOf(binding), l);

        verify();
    }
}
