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

package org.apache.tapestry.internal.test;

import org.apache.tapestry.EventContext;
import org.apache.tapestry.dom.Document;
import org.apache.tapestry.internal.URLEventContext;
import org.apache.tapestry.internal.services.ComponentInvocation;
import org.apache.tapestry.internal.services.InvocationTarget;
import org.apache.tapestry.internal.services.PageLinkTarget;
import org.apache.tapestry.ioc.Registry;
import org.apache.tapestry.services.ContextValueEncoder;
import org.apache.tapestry.services.PageRenderRequestHandler;
import org.apache.tapestry.services.PageRenderRequestParameters;

import java.io.IOException;

/**
 * Simulates a click on a page link.
 */
public class PageLinkInvoker implements ComponentInvoker
{
    private final Registry registry;

    private final PageRenderRequestHandler pageRenderRequestHandler;

    private final TestableMarkupWriterFactory markupWriterFactory;

    private final TestableResponse response;

    private final ContextValueEncoder contextValueEncoder;

    public PageLinkInvoker(Registry registry)
    {
        this.registry = registry;

        pageRenderRequestHandler = this.registry.getService(PageRenderRequestHandler.class);
        markupWriterFactory = this.registry.getService(TestableMarkupWriterFactory.class);
        response = this.registry.getService(TestableResponse.class);
        contextValueEncoder = this.registry.getService(ContextValueEncoder.class);
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

            PageLinkTarget pageLinkTarget = (PageLinkTarget) target;

            EventContext activationContext
                    = new URLEventContext(contextValueEncoder, invocation.getContext());
            PageRenderRequestParameters parameters = new PageRenderRequestParameters(pageLinkTarget.getPageName(),
                                                                                     activationContext);

            pageRenderRequestHandler.handle(parameters);

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
