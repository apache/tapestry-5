// Copyright 2006, 2007, 2008, 2009, 2010, 2011 The Apache Software Foundation
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

import org.apache.tapestry5.beanmodel.services.*;
import org.apache.tapestry5.http.services.Request;
import org.apache.tapestry5.internal.InternalConstants;
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
@SuppressWarnings("unchecked")
public class PageRenderRequestHandlerImpl implements PageRenderRequestHandler
{
    private final RequestPageCache cache;

    private final ComponentEventResultProcessor resultProcessor;

    private final PageResponseRenderer pageResponseRenderer;

    private final PageActivator pageActivator;

    private final Request request;

    public PageRenderRequestHandlerImpl(RequestPageCache cache, @Traditional
    @Primary
    ComponentEventResultProcessor resultProcessor, PageResponseRenderer pageResponseRenderer,
                                        PageActivator pageActivator, Request request)
    {
        this.cache = cache;
        this.resultProcessor = resultProcessor;
        this.pageResponseRenderer = pageResponseRenderer;
        this.pageActivator = pageActivator;
        this.request = request;
    }

    public void handle(PageRenderRequestParameters parameters) throws IOException
    {
        Page page = cache.get(parameters.getLogicalPageName());

        if (request.getAttribute(InternalConstants.BYPASS_ACTIVATION) == null)
        {
            if (pageActivator.activatePage(page.getRootElement().getComponentResources(),
                    parameters.getActivationContext(), resultProcessor))
            {
                return;
            }

            if (!parameters.isLoopback())
            {
                page.pageReset();
            }
        }

        pageResponseRenderer.renderPageResponse(page);
    }
}
