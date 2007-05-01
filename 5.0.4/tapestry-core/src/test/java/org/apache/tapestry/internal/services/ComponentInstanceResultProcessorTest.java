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

import org.apache.commons.logging.Log;
import org.apache.tapestry.ComponentResources;
import org.apache.tapestry.Link;
import org.apache.tapestry.internal.structure.Page;
import org.apache.tapestry.internal.test.InternalBaseTestCase;
import org.apache.tapestry.runtime.Component;
import org.apache.tapestry.services.ComponentEventResultProcessor;
import org.apache.tapestry.services.Response;
import org.testng.annotations.Test;

public class ComponentInstanceResultProcessorTest extends InternalBaseTestCase
{
    private static final String PAGE_NAME = "foo.bar.Zoop";

    private static final String METHOD_DESCRIPTION = "foo.bar.Baz.biff()";

    private static final String LINK_URI = "{LinkURI}";

    @Test
    public void result_is_root_component() throws Exception
    {
        Component result = mockComponent();
        Component source = mockComponent();
        ComponentResources resources = mockComponentResources();
        Log log = mockLog();
        RequestPageCache cache = mockRequestPageCache();
        Page page = mockPage();
        LinkFactory factory = mockLinkFactory();
        Response response = mockResponse();
        Link link = mockLink();

        train_getComponentResources(result, resources);
        train_getContainer(resources, null);

        train_getCompleteId(resources, PAGE_NAME);
        train_getByClassName(cache, PAGE_NAME, page);

        train_createPageLink(factory, page, link);
        train_toRedirectURI(link, LINK_URI);

        response.sendRedirect(LINK_URI);

        replay();

        ComponentEventResultProcessor<Component> processor = new ComponentInstanceResultProcessor(
                cache, factory, log);

        processor.processComponentEvent(result, source, METHOD_DESCRIPTION).sendClientResponse(
                response);

        verify();
    }

    @Test
    public void warning_for_component_is_not_root_component() throws Exception
    {
        Component child = mockComponent();
        Component source = mockComponent();
        Component root = mockComponent();
        Component container = mockComponent();
        ComponentResources rootResources = mockComponentResources();
        ComponentResources childResources = mockComponentResources();
        ComponentResources sourceResources = mockComponentResources();
        Log log = mockLog();
        RequestPageCache cache = mockRequestPageCache();
        Page page = mockPage();
        LinkFactory factory = mockLinkFactory();
        Response response = mockResponse();
        Link link = mockLink();

        train_getComponentResources(child, childResources);

        train_getContainer(childResources, container);

        train_getComponentResources(source, sourceResources);

        train_getCompleteId(sourceResources, PAGE_NAME + ":source");
        train_getCompleteId(childResources, PAGE_NAME + ":child");

        train_getPage(childResources, root);
        train_getComponentResources(root, rootResources);

        train_getCompleteId(rootResources, PAGE_NAME);
        train_getByClassName(cache, PAGE_NAME, page);

        log
                .warn("Method foo.bar.Baz.biff() (for component foo.bar.Zoop:source) returned component foo.bar.Zoop:child, which is not a page component. The page containing the component will render the client response.");

        train_createPageLink(factory, page, link);
        train_toRedirectURI(link, LINK_URI);

        response.sendRedirect(LINK_URI);

        replay();

        ComponentEventResultProcessor<Component> processor = new ComponentInstanceResultProcessor(
                cache, factory, log);

        processor.processComponentEvent(child, source, METHOD_DESCRIPTION).sendClientResponse(
                response);

        verify();
    }
}
