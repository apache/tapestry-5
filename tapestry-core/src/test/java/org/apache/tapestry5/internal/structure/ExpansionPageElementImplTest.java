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

package org.apache.tapestry5.internal.structure;

import org.apache.tapestry5.Binding;
import org.apache.tapestry5.MarkupWriter;
import org.apache.tapestry5.internal.test.InternalBaseTestCase;
import org.apache.tapestry5.ioc.services.TypeCoercer;
import org.apache.tapestry5.runtime.RenderCommand;
import org.apache.tapestry5.runtime.RenderQueue;
import org.testng.annotations.Test;

public class ExpansionPageElementImplTest extends InternalBaseTestCase
{
    @Test
    public void invariant_binding_is_cached()
    {
        Binding binding = mockBinding();
        TypeCoercer coercer = mockTypeCoercer();
        MarkupWriter writer = mockMarkupWriter();
        RenderQueue queue = mockRenderQueue();

        Object value = new Object();

        train_isInvariant(binding, true);

        replay();

        RenderCommand element = new ExpansionPageElement(binding, coercer);

        verify();

        train_get(binding, value);
        train_coerce(coercer, value, String.class, "STRING-VALUE");
        writer.write("STRING-VALUE");

        replay();

        element.render(writer, queue);

        verify();

        // It is now cached ...

        writer.write("STRING-VALUE");

        replay();

        element.render(writer, queue);

        verify();
    }

    @Test
    public void variant_binding_is_not_cached()
    {
        Binding binding = mockBinding();
        TypeCoercer coercer = mockTypeCoercer();
        MarkupWriter writer = mockMarkupWriter();
        RenderQueue queue = mockRenderQueue();

        Object value = new Object();

        train_isInvariant(binding, false);

        replay();

        RenderCommand element = new ExpansionPageElement(binding, coercer);

        verify();

        train_get(binding, value);
        train_coerce(coercer, value, String.class, "STRING-VALUE");
        writer.write("STRING-VALUE");

        replay();

        element.render(writer, queue);

        verify();

        train_get(binding, value);
        train_coerce(coercer, value, String.class, "STRING-VALUE2");
        writer.write("STRING-VALUE2");

        replay();

        element.render(writer, queue);

        verify();
    }
}
