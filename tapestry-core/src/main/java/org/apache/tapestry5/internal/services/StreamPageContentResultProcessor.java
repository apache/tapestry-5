// Copyright 2010-2014 The Apache Software Foundation
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
import org.apache.tapestry5.TapestryConstants;
import org.apache.tapestry5.beanmodel.services.*;
import org.apache.tapestry5.commons.services.TypeCoercer;
import org.apache.tapestry5.http.services.Request;
import org.apache.tapestry5.http.services.RequestGlobals;
import org.apache.tapestry5.internal.EmptyEventContext;
import org.apache.tapestry5.internal.InternalConstants;
import org.apache.tapestry5.ioc.IOOperation;
import org.apache.tapestry5.services.ComponentClassResolver;
import org.apache.tapestry5.services.ComponentEventResultProcessor;
import org.apache.tapestry5.services.PageRenderRequestHandler;
import org.apache.tapestry5.services.PageRenderRequestParameters;
import org.apache.tapestry5.services.StreamPageContent;

import java.io.IOException;

/**
 * Used to trigger the rendering of a particular page without causing a redirect to that page.
 * The content of the page is just streamed to the client.
 *
 * @since 5.2.0
 */
public class StreamPageContentResultProcessor implements ComponentEventResultProcessor<StreamPageContent>
{
    private final PageRenderRequestHandler handler;

    private final ComponentClassResolver resolver;

    private final TypeCoercer typeCoercer;

    private final RequestGlobals requestGlobals;

    private final Request request;

    public StreamPageContentResultProcessor(PageRenderRequestHandler handler, ComponentClassResolver resolver, TypeCoercer typeCoercer, RequestGlobals requestGlobals, Request request)
    {
        this.handler = handler;
        this.resolver = resolver;
        this.typeCoercer = typeCoercer;
        this.requestGlobals = requestGlobals;
        this.request = request;
    }

    public void processResultValue(StreamPageContent value) throws IOException
    {
        Class<?> pageClass = value.getPageClass();
        Object[] activationContext = value.getPageActivationContext();

        final String pageName = pageClass == null
                ? requestGlobals.getActivePageName()
                : resolver.resolvePageClassNameToPageName(pageClass.getName());

        final EventContext context = activationContext == null
                ? new EmptyEventContext()
                : new ArrayEventContext(typeCoercer, activationContext);

        if (value.isBypassActivation())
        {
            request.setAttribute(InternalConstants.BYPASS_ACTIVATION, true);
        }

        request.setAttribute(TapestryConstants.RESPONSE_RENDERER, new IOOperation<Void>()
        {
            public Void perform() throws IOException
            {
                handler.handle(new PageRenderRequestParameters(pageName, context, false));

                return null;
            }
        });
    }
}
