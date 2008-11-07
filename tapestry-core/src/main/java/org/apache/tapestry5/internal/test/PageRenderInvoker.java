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

package org.apache.tapestry5.internal.test;

import org.apache.tapestry5.Link;
import org.apache.tapestry5.dom.Document;
import org.apache.tapestry5.internal.services.ComponentInvocation;
import org.apache.tapestry5.internal.services.ComponentInvocationMap;
import org.apache.tapestry5.internal.services.InvocationTarget;
import org.apache.tapestry5.internal.services.PageRenderTarget;
import org.apache.tapestry5.ioc.Registry;
import org.apache.tapestry5.services.PageRenderRequestHandler;
import org.apache.tapestry5.services.PageRenderRequestParameters;

import java.io.IOException;

/**
 * Simulates a click on a page link.
 */
public class PageRenderInvoker implements ComponentInvoker
{
    private final Registry registry;

    private final PageRenderRequestHandler pageRenderRequestHandler;

    private final TestableMarkupWriterFactory markupWriterFactory;

    private final TestableResponse response;

    private final ComponentInvoker followupInvoker;

    private final ComponentInvocationMap componentInvocationMap;

    public PageRenderInvoker(Registry registry, ComponentInvoker followupInvoker,
                             ComponentInvocationMap componentInvocationMap)
    {
        this.registry = registry;
        this.followupInvoker = followupInvoker;
        this.componentInvocationMap = componentInvocationMap;

        pageRenderRequestHandler = this.registry.getService(PageRenderRequestHandler.class);
        markupWriterFactory = this.registry.getService(TestableMarkupWriterFactory.class);
        response = this.registry.getService(TestableResponse.class);
    }

    /**
     * Click on the page link.
     *
     * @param invocation The ComponentInvocation object corresponding to the page link.
     * @return The DOM created. Typically you will assert against it.
     */
    public Document invoke(ComponentInvocation invocation)
    {
        try
        {
            InvocationTarget target = invocation.getTarget();

            PageRenderTarget pageRenderTarget = (PageRenderTarget) target;

            PageRenderRequestParameters parameters = new PageRenderRequestParameters(pageRenderTarget.getPageName(),
                                                                                     invocation.getPageActivationContext());

            pageRenderRequestHandler.handle(parameters);

            Link redirect = response.getRedirectLink();

            if (redirect != null)
            {

                ComponentInvocation followup = componentInvocationMap.get(redirect);

                response.clear();

                return followupInvoker.invoke(followup);
            }

            return markupWriterFactory.getLatestMarkupWriter().getDocument();
        }
        catch (IOException ex)
        {
            throw new RuntimeException(ex);
        }
        finally
        {
            response.clear();

            registry.cleanupThread();
        }
    }
}
