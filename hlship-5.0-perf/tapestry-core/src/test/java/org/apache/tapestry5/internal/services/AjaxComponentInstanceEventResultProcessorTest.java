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

package org.apache.tapestry5.internal.services;

import org.apache.tapestry5.ComponentResources;
import org.apache.tapestry5.internal.structure.ComponentPageElement;
import org.apache.tapestry5.internal.structure.Page;
import org.apache.tapestry5.internal.test.InternalBaseTestCase;
import org.apache.tapestry5.runtime.Component;
import org.apache.tapestry5.services.ComponentEventResultProcessor;
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
        Page page = mockPage();
        ComponentResources resources = mockComponentResources();
        Component component = mockComponent();
        Component pageComponent = mockComponent();

        ComponentPageElement element = mockComponentPageElement();
        ComponentEventResultProcessor master = mockComponentEventResultProcessor();

        train_getComponentResources(component, resources);
        train_getPage(resources, pageComponent);
        train_getPageName(resources, pageName);
        train_get(cache, pageName, page);
        train_getNestedId(resources, nestedId);
        train_getComponentElementByNestedId(page, nestedId, element);

        master.processResultValue(element);

        replay();

        ComponentEventResultProcessor<Component> processor = new AjaxComponentInstanceEventResultProcessor(
                cache, master);

        processor.processResultValue(component);

        verify();
    }

    @Test
    public void render_complete_page_as_partial() throws IOException
    {
        String pageName = "Biff";

        RequestPageCache cache = mockRequestPageCache();
        ComponentResources resources = mockComponentResources();
        Component component = mockComponent();
        PageRenderQueue queue = mockPageRenderQueue();
        ComponentEventResultProcessor master = mockComponentEventResultProcessor();

        train_getComponentResources(component, resources);
        train_getPage(resources, component);
        train_getPageName(resources, pageName);

        master.processResultValue(pageName);

        replay();

        ComponentEventResultProcessor<Component> processor = new AjaxComponentInstanceEventResultProcessor(
                cache,
                master);

        processor.processResultValue(component);

        verify();
    }
}
