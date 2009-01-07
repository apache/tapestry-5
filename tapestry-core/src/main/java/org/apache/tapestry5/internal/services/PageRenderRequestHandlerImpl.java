// Copyright 2006, 2007, 2008, 2009 The Apache Software Foundation
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

import org.apache.tapestry5.EventConstants;
import org.apache.tapestry5.internal.structure.Page;
import org.apache.tapestry5.ioc.annotations.Primary;
import org.apache.tapestry5.services.ComponentEventResultProcessor;
import org.apache.tapestry5.services.PageRenderRequestHandler;
import org.apache.tapestry5.services.PageRenderRequestParameters;
import org.apache.tapestry5.services.Traditional;

import java.io.IOException;

/**
 * Handles a page render request by activating and then rendering the page.
 *
 * @see org.apache.tapestry5.internal.services.PageRenderDispatcher
 */
public class PageRenderRequestHandlerImpl implements PageRenderRequestHandler
{
    private final RequestPageCache cache;

    private final ComponentEventResultProcessor resultProcessor;

    private final PageResponseRenderer pageResponseRenderer;

    public PageRenderRequestHandlerImpl(RequestPageCache cache,
                                        @Traditional @Primary
                                        ComponentEventResultProcessor resultProcessor,
                                        PageResponseRenderer pageResponseRenderer)
    {
        this.cache = cache;
        this.resultProcessor = resultProcessor;
        this.pageResponseRenderer = pageResponseRenderer;
    }

    public void handle(PageRenderRequestParameters parameters) throws IOException
    {
        Page page = cache.get(parameters.getLogicalPageName());

        ComponentResultProcessorWrapper callback = new ComponentResultProcessorWrapper(resultProcessor);

        page.getRootElement().triggerContextEvent(EventConstants.ACTIVATE, parameters.getActivationContext(),
                                                  callback);

        // The handler will have asked the result processor to send a response.

        if (callback.isAborted()) return;

        pageResponseRenderer.renderPageResponse(page);
    }
}
