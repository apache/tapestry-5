// Copyright 2006, 2007, 2008, 2011 The Apache Software Foundation
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
import org.apache.tapestry5.internal.test.InternalBaseTestCase;
import org.apache.tapestry5.runtime.Component;
import org.apache.tapestry5.services.ComponentEventResultProcessor;
import org.slf4j.Logger;
import org.testng.annotations.Test;

public class ComponentInstanceResultProcessorTest extends InternalBaseTestCase
{
    private static final String PAGE_NAME = "Zoop";

    @Test
    public void result_is_root_component() throws Exception
    {
        Component result = mockComponent();
        ComponentResources resources = mockComponentResources();
        Logger logger = mockLogger();
        ComponentEventResultProcessor primary = mockComponentEventResultProcessor();

        train_getComponentResources(result, resources);
        train_getContainer(resources, null);

        train_getPageName(resources, PAGE_NAME);

        primary.processResultValue(PAGE_NAME);

        replay();

        ComponentEventResultProcessor<Component> processor = new ComponentInstanceResultProcessor(logger,
                primary);

        processor.processResultValue(result);

        verify();
    }

    @Test
    public void warning_for_component_is_not_root_component() throws Exception
    {
        Component value = mockComponent();
        Component containerResources = mockComponent();
        ComponentResources valueResources = mockComponentResources();
        Logger logger = mockLogger();
        ComponentEventResultProcessor primary = mockComponentEventResultProcessor();

        train_getComponentResources(value, valueResources);

        train_getContainer(valueResources, containerResources);

        String completeId = PAGE_NAME + ":child";

        train_getCompleteId(valueResources, completeId);

        logger
                .warn("Component {} was returned from an event handler method, but is not a page component. The page containing the component will render the client response.", completeId);

        train_getPageName(valueResources, PAGE_NAME);

        primary.processResultValue(PAGE_NAME);

        replay();

        ComponentEventResultProcessor<Component> processor = new ComponentInstanceResultProcessor(logger, primary);

        processor.processResultValue(value);

        verify();
    }

}
