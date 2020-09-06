// Copyright 2010, 2011 The Apache Software Foundation
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.apache.tapestry5.internal.services;

import org.apache.tapestry5.EventContext;
import org.apache.tapestry5.http.services.Request;
import org.apache.tapestry5.internal.InternalComponentResources;
import org.apache.tapestry5.internal.InternalConstants;
import org.apache.tapestry5.internal.structure.ComponentPageElement;
import org.apache.tapestry5.internal.structure.Page;
import org.apache.tapestry5.internal.test.InternalBaseTestCase;
import org.apache.tapestry5.services.ComponentEventResultProcessor;
import org.apache.tapestry5.services.PageRenderRequestHandler;
import org.apache.tapestry5.services.PageRenderRequestParameters;
import org.testng.annotations.Test;

public class PageRenderRequestHandlerImplTest extends InternalBaseTestCase
{
    @Test
    public void loopback_request() throws Exception
    {
        RequestPageCache cache = mockRequestPageCache();
        ComponentEventResultProcessor processor = mockComponentEventResultProcessor();
        PageResponseRenderer renderer = mockPageResponseRenderer();
        Page page = mockPage();
        EventContext context = mockEventContext();
        ComponentPageElement root = mockComponentPageElement();
        InternalComponentResources pageResources = mockInternalComponentResources();
        PageActivator activator = newMock(PageActivator.class);
        Request request = mockRequest();
        
        train_getAttribute(request, InternalConstants.BYPASS_ACTIVATION, null);
        train_get(cache, "foo/Bar", page);

        train_getRootElement(page, root);
        train_getComponentResources(root, pageResources);
        expect(activator.activatePage(pageResources, context, processor)).andReturn(false);

        // Skips the pageReset()

        renderer.renderPageResponse(page);

        replay();

        PageRenderRequestHandler handler = new PageRenderRequestHandlerImpl(cache, processor, renderer, activator, request);

        PageRenderRequestParameters parameters = new PageRenderRequestParameters("foo/Bar", context, true);

        handler.handle(parameters);

        verify();
    }
}
