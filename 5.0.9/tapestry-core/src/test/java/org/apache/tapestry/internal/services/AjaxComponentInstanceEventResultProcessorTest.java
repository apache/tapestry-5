// Copyright 2007, 2008 The Apache Software Foundation
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
import org.apache.tapestry.internal.structure.ComponentPageElement;
import org.apache.tapestry.internal.structure.Page;
import org.apache.tapestry.internal.test.InternalBaseTestCase;
import org.apache.tapestry.runtime.Component;
import org.apache.tapestry.services.ComponentEventResultProcessor;
import org.testng.annotations.Test;

import java.io.IOException;

public class AjaxComponentInstanceEventResultProcessorTest extends InternalBaseTestCase
{
    @Test
    public void render_component_within_page() throws IOException
    {
        String nestedId = "foo.bar.baz";
        String pageName = "Biff";

        RequestPageCache cache = mockRequestPageCache();
        AjaxPartialResponseRenderer renderer = newMock(AjaxPartialResponseRenderer.class);
        Page page = mockPage();
        ComponentResources resources = mockComponentResources();
        Component component = mockComponent();
        ComponentPageElement element = mockComponentPageElement();


        train_getComponentResources(component, resources);
        train_getPageName(resources, pageName);
        train_get(cache, pageName, page);
        train_getNestedId(resources, nestedId);
        train_getComponentElementByNestedId(page, nestedId, element);

        renderer.renderPartialPageMarkup(element);

        replay();

        ComponentEventResultProcessor<Component> processor = new AjaxComponentInstanceEventResultProcessor(renderer,
                                                                                                           cache);

        processor.processResultValue(component, null, null);

        verify();
    }

    @Test
    public void render_complete_page_as_partial() throws IOException
    {
        String pageName = "Biff";

        RequestPageCache cache = mockRequestPageCache();
        AjaxPartialResponseRenderer renderer = newMock(AjaxPartialResponseRenderer.class);
        Page page = mockPage();
        ComponentResources resources = mockComponentResources();
        Component component = mockComponent();
        ComponentPageElement element = mockComponentPageElement();


        train_getComponentResources(component, resources);
        train_getPageName(resources, pageName);
        train_get(cache, pageName, page);
        train_getNestedId(resources, null);
        train_getRootElement(page, element);

        renderer.renderPartialPageMarkup(element);

        replay();

        ComponentEventResultProcessor<Component> processor = new AjaxComponentInstanceEventResultProcessor(renderer,
                                                                                                           cache);

        processor.processResultValue(component, null, null);

        verify();
    }
}
