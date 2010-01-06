// Copyright 2010 The Apache Software Foundation
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

import org.apache.tapestry5.ComponentEventCallback;
import org.apache.tapestry5.EventConstants;
import org.apache.tapestry5.EventContext;
import org.apache.tapestry5.internal.InternalConstants;
import org.apache.tapestry5.internal.structure.ComponentPageElement;
import org.apache.tapestry5.internal.structure.Page;
import org.apache.tapestry5.internal.test.InternalBaseTestCase;
import org.apache.tapestry5.services.ComponentEventResultProcessor;
import org.apache.tapestry5.services.PageRenderRequestHandler;
import org.apache.tapestry5.services.PageRenderRequestParameters;
import org.apache.tapestry5.services.Request;
import org.easymock.EasyMock;
import org.testng.annotations.Test;

public class PageRenderRequestHandlerImplTest extends InternalBaseTestCase
{
    @Test
    public void loopback_request() throws Exception
    {
        RequestPageCache cache = mockRequestPageCache();
        Request request = mockRequest();
        ComponentEventResultProcessor processor = mockComponentEventResultProcessor();
        PageResponseRenderer renderer = mockPageResponseRenderer();
        Page page = mockPage();
        EventContext context = mockEventContext();
        ComponentPageElement root = mockComponentPageElement();

        train_get(cache, "foo/Bar", page);

        train_getRootElement(page, root);

        expect(
                root.triggerContextEvent(EasyMock.eq(EventConstants.ACTIVATE), EasyMock
                        .same(context), EasyMock.isA(ComponentEventCallback.class))).andReturn(
                false);

        train_getParameter(request, InternalConstants.LOOPBACK, "T");

        // Skips the pageReset()

        renderer.renderPageResponse(page);

        replay();

        PageRenderRequestHandler handler = new PageRenderRequestHandlerImpl(cache, processor,
                renderer, request);

        PageRenderRequestParameters parameters = new PageRenderRequestParameters("foo/Bar", context);

        handler.handle(parameters);

        verify();
    }
}
