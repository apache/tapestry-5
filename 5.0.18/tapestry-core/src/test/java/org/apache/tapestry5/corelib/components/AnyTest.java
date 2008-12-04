// Copyright 2008 The Apache Software Foundation
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

package org.apache.tapestry5.corelib.components;

import org.apache.tapestry5.ComponentResources;
import org.apache.tapestry5.MarkupWriter;
import org.apache.tapestry5.RenderSupport;
import org.apache.tapestry5.dom.DefaultMarkupModel;
import org.apache.tapestry5.internal.services.MarkupWriterImpl;
import org.apache.tapestry5.test.TapestryTestCase;
import org.testng.annotations.Test;

public class AnyTest extends TapestryTestCase
{
    @Test
    public void render_simple()
    {
        ComponentResources resources = mockComponentResources();
        RenderSupport support = mockRenderSupport();

        MarkupWriter writer = new MarkupWriterImpl(new DefaultMarkupModel());

        resources.renderInformalParameters(writer);

        replay();

        Any component = new Any();
        component.inject(support, resources, "span", "foo");

        component.beginRender(writer);
        writer.write("content");
        component.afterRender(writer);

        assertEquals(writer.toString(), "<span>content</span>");

        verify();
    }

    @Test
    public void render_with_id()
    {
        ComponentResources resources = mockComponentResources();
        RenderSupport support = mockRenderSupport();

        MarkupWriter writer = new MarkupWriterImpl(new DefaultMarkupModel());

        resources.renderInformalParameters(writer);

        String clientId = "bar";
        String uniqueId = "bar_0";

        expect(support.allocateClientId(clientId)).andReturn(uniqueId);

        replay();

        Any component = new Any();
        component.inject(support, resources, "div", clientId);

        component.beginRender(writer);
        writer.write("content");
        component.afterRender(writer);

        assertEquals(writer.toString(), "<div>content</div>");

        assertEquals(component.getClientId(), uniqueId);

        assertEquals(writer.toString(), "<div id=\"bar_0\">content</div>");

        assertEquals(component.getClientId(), uniqueId);

        verify();

    }
}
