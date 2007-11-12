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

package org.apache.tapestry.internal.services;

import org.apache.tapestry.ComponentResources;
import org.apache.tapestry.MarkupWriter;
import org.apache.tapestry.dom.MarkupModel;
import org.apache.tapestry.dom.XMLMarkupModel;
import org.apache.tapestry.internal.parser.AttributeToken;
import org.apache.tapestry.internal.parser.StartElementToken;
import org.apache.tapestry.internal.parser.TextToken;
import org.apache.tapestry.internal.structure.ComponentPageElement;
import org.apache.tapestry.internal.structure.PageElement;
import org.apache.tapestry.internal.test.InternalBaseTestCase;
import org.apache.tapestry.ioc.Location;
import org.apache.tapestry.ioc.internal.util.TapestryException;
import org.apache.tapestry.ioc.services.TypeCoercer;
import org.apache.tapestry.runtime.RenderQueue;
import org.apache.tapestry.services.BindingSource;
import org.apache.tapestry.services.ComponentClassResolver;
import org.apache.tapestry.services.ComponentMessagesSource;
import org.testng.annotations.Test;

public class PageElementFactoryImplTest extends InternalBaseTestCase
{
    private static MarkupModel _xmlModel = new XMLMarkupModel();

    @Test
    public void start_element()
    {
        ComponentInstantiatorSource source = mockComponentInstantiatorSource();
        ComponentClassResolver resolver = mockComponentClassResolver();
        MarkupWriter writer = new MarkupWriterImpl();
        Location l = mockLocation();
        RenderQueue queue = mockRenderQueue();

        replay();

        PageElementFactory factory = new PageElementFactoryImpl(source, resolver, null, null, null);
        StartElementToken token = new StartElementToken("fred", l);

        PageElement element = factory.newStartElement(token);

        element.render(writer, queue);

        verify();

        assertEquals(element.toString(), "Start[fred]");
        assertEquals(writer.toString(), "<fred></fred>");
    }

    @Test
    public void attribute()
    {
        ComponentInstantiatorSource source = mockComponentInstantiatorSource();
        ComponentClassResolver resolver = mockComponentClassResolver();
        MarkupWriter writer = new MarkupWriterImpl(_xmlModel, null);
        Location l = mockLocation();
        RenderQueue queue = mockRenderQueue();

        replay();

        PageElementFactory factory = new PageElementFactoryImpl(source, resolver, null, null, null);
        AttributeToken token = new AttributeToken("name", "value", l);

        PageElement element = factory.newAttributeElement(null, token);

        writer.element("root");

        element.render(writer, queue);

        verify();

        assertEquals(writer.toString(), "<root name=\"value\"/>");
    }

    @Test
    public void end_element()
    {
        ComponentInstantiatorSource source = mockComponentInstantiatorSource();
        ComponentClassResolver resolver = mockComponentClassResolver();
        MarkupWriter writer = new MarkupWriterImpl(_xmlModel, null);
        RenderQueue queue = mockRenderQueue();

        replay();

        PageElementFactory factory = new PageElementFactoryImpl(source, resolver, null, null, null);

        PageElement element = factory.newEndElement();

        writer.element("root");
        writer.write("before");
        writer.element("nested");

        element.render(writer, queue);

        writer.write("after");

        verify();

        assertEquals(element.toString(), "End");
        assertEquals(writer.toString(), "<root>before<nested/>after</root>");
    }

    @Test
    public void end_element_is_singleton()
    {
        ComponentInstantiatorSource source = mockComponentInstantiatorSource();
        ComponentClassResolver resolver = mockComponentClassResolver();

        replay();

        PageElementFactory factory = new PageElementFactoryImpl(source, resolver, null, null, null);

        PageElement element1 = factory.newEndElement();
        PageElement element2 = factory.newEndElement();

        assertSame(element2, element1);

        verify();
    }

    @Test
    public void text_element()
    {
        ComponentInstantiatorSource source = mockComponentInstantiatorSource();
        ComponentClassResolver resolver = mockComponentClassResolver();
        MarkupWriter writer = new MarkupWriterImpl();
        Location l = mockLocation();
        RenderQueue queue = mockRenderQueue();

        replay();

        PageElementFactory factory = new PageElementFactoryImpl(source, resolver, null, null, null);
        TextToken token = new TextToken("some text", l);

        PageElement element = factory.newTextElement(token);

        writer.element("root");
        element.render(writer, queue);

        verify();

        assertEquals(element.toString(), "Text[some text]");
        assertEquals(writer.toString(), "<root>some text</root>");
    }

    @Test
    public void render_body_element()
    {
        ComponentInstantiatorSource source = mockComponentInstantiatorSource();
        ComponentClassResolver resolver = mockComponentClassResolver();
        RenderQueue queue = mockRenderQueue();
        ComponentPageElement component = mockComponentPageElement();
        MarkupWriter writer = newMock(MarkupWriter.class);

        component.enqueueBeforeRenderBody(queue);

        replay();

        PageElementFactory factory = new PageElementFactoryImpl(source, resolver, null, null, null);

        PageElement element = factory.newRenderBodyElement(component);

        element.render(writer, queue);

        verify();
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

        AttributeToken token = new AttributeToken("fred", "${flintstone", location);

        replay();

        PageElementFactory factory = new PageElementFactoryImpl(source, resolver, typeCoercer,
                                                                bindingSource, messagesSource);

        try
        {
            factory.newAttributeElement(resources, token);
            unreachable();
        }
        catch (TapestryException ex)
        {
            assertEquals(
                    ex.getMessage(),
                    "Attribute expression \'${flintstone\' is missing a closing brace.");
            assertSame(ex.getLocation(), location);
        }

        verify();
    }
}
