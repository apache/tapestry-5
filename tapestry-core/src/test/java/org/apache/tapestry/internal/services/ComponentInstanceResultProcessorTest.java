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

package org.apache.tapestry.internal.services;

import org.apache.tapestry.ComponentResources;
import org.apache.tapestry.internal.structure.Page;
import org.apache.tapestry.internal.test.InternalBaseTestCase;
import org.apache.tapestry.runtime.Component;
import org.apache.tapestry.services.ComponentEventResultProcessor;
import org.slf4j.Logger;
import org.testng.annotations.Test;

public class ComponentInstanceResultProcessorTest extends InternalBaseTestCase
{
    private static final String PAGE_NAME = "Zoop";

    private static final String METHOD_DESCRIPTION = "foo.bar.Baz.biff()";

    @Test
    public void result_is_root_component() throws Exception
    {
        Component result = mockComponent();
        Component source = mockComponent();
        ComponentResources resources = mockComponentResources();
        Logger logger = mockLogger();
        RequestPageCache cache = mockRequestPageCache();
        Page page = mockPage();
        ActionRenderResponseGenerator generator = mockActionRenderResponseGenerator();

        train_getComponentResources(result, resources);
        train_getContainer(resources, null);

        train_getPageName(resources, PAGE_NAME);
        train_get(cache, PAGE_NAME, page);

        generator.generateResponse(page);

        replay();

        ComponentEventResultProcessor<Component> processor = new ComponentInstanceResultProcessor(logger, cache,
                                                                                                  generator);

        processor.processComponentEvent(result, source, METHOD_DESCRIPTION);

        verify();
    }

    @Test
    public void warning_for_component_is_not_root_component() throws Exception
    {
        Component value = mockComponent();
        Component source = mockComponent();
        Component containerResources = mockComponent();
        ComponentResources valueResources = mockComponentResources();
        ComponentResources sourceResources = mockComponentResources();
        Logger logger = mockLogger();
        RequestPageCache cache = mockRequestPageCache();
        Page page = mockPage();
        ActionRenderResponseGenerator generator = mockActionRenderResponseGenerator();


        train_getComponentResources(value, valueResources);

        train_getContainer(valueResources, containerResources);
        train_getComponentResources(source, sourceResources);

        train_getCompleteId(sourceResources, PAGE_NAME + ":source");
        train_getCompleteId(valueResources, PAGE_NAME + ":child");

        logger
                .warn("Method foo.bar.Baz.biff() (for component Zoop:source) returned component Zoop:child, which is not a page component. The page containing the component will render the client response.");

        train_getPageName(valueResources, PAGE_NAME);
        train_get(cache, PAGE_NAME, page);

        generator.generateResponse(page);

        replay();

        ComponentEventResultProcessor<Component> processor = new ComponentInstanceResultProcessor(logger, cache,
                                                                                                  generator);

        processor.processComponentEvent(value, source, METHOD_DESCRIPTION);

        verify();
    }

}
