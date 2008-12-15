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

package org.apache.tapestry5.internal.services;

import org.apache.tapestry5.ComponentResources;
import org.apache.tapestry5.MarkupWriter;
import org.apache.tapestry5.dom.MarkupModel;
import org.apache.tapestry5.dom.XMLMarkupModel;
import org.apache.tapestry5.internal.parser.AttributeToken;
import org.apache.tapestry5.internal.structure.PageElement;
import org.apache.tapestry5.internal.test.InternalBaseTestCase;
import org.apache.tapestry5.ioc.Location;
import org.apache.tapestry5.ioc.internal.util.TapestryException;
import org.apache.tapestry5.ioc.services.TypeCoercer;
import org.apache.tapestry5.runtime.RenderQueue;
import org.apache.tapestry5.services.BindingSource;
import org.apache.tapestry5.services.ComponentClassResolver;
import org.testng.annotations.Test;

public class PageElementFactoryImplTest extends InternalBaseTestCase
{
    private static MarkupModel xmlModel = new XMLMarkupModel();

    @Test
    public void attribute()
    {
        ComponentInstantiatorSource source = mockComponentInstantiatorSource();
        ComponentClassResolver resolver = mockComponentClassResolver();
        MarkupWriter writer = new MarkupWriterImpl(xmlModel);
        Location l = mockLocation();
        RenderQueue queue = mockRenderQueue();

        replay();

        PageElementFactory factory = new PageElementFactoryImpl(source, resolver, null, null, null);
        AttributeToken token = new AttributeToken(null, "name", "value", l);

        PageElement element = factory.newAttributeElement(null, token);

        writer.element("root");

        element.render(writer, queue);

        verify();

        assertEquals(writer.toString(), "<?xml version=\"1.0\"?>\n<root name=\"value\"/>");
    }


    @Test
    public void unclosed_attribute_expression()
    {
        ComponentInstantiatorSource source = mockComponentInstantiatorSource();
        ComponentClassResolver resolver = mockComponentClassResolver();
        TypeCoercer typeCoercer = mockTypeCoercer();
        BindingSource bindingSource = mockBindingSource();
        ComponentMessagesSource messagesSource = newMock(ComponentMessagesSource.class);
        ComponentResources resources = mockComponentResources();
        Location location = mockLocation();

        AttributeToken token = new AttributeToken(null, "fred", "${flintstone", location);

        replay();

        PageElementFactory factory = new PageElementFactoryImpl(source, resolver, typeCoercer, bindingSource, null);

        try
        {
            factory.newAttributeElement(resources, token);
            unreachable();
        }
        catch (TapestryException ex)
        {
            assertEquals(ex.getMessage(), "Attribute expression \'${flintstone\' is missing a closing brace.");
            assertSame(ex.getLocation(), location);
        }

        verify();
    }
}
