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

import java.io.IOException;

import org.apache.tapestry5.EventContext;
import org.apache.tapestry5.internal.EmptyEventContext;
import org.apache.tapestry5.internal.InternalConstants;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.ioc.services.TypeCoercer;
import org.apache.tapestry5.services.ComponentClassResolver;
import org.apache.tapestry5.services.ComponentEventResultProcessor;
import org.apache.tapestry5.services.PageRenderRequestHandler;
import org.apache.tapestry5.services.PageRenderRequestParameters;
import org.apache.tapestry5.services.Request;
import org.apache.tapestry5.services.StreamPageContent;

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

    public StreamPageContentResultProcessor(PageRenderRequestHandler handler, ComponentClassResolver resolver, TypeCoercer typeCoercer)
    {
        this.handler = handler;
        this.resolver = resolver;
        this.typeCoercer = typeCoercer;
    }

    public void processResultValue(final StreamPageContent value) throws IOException
    {

        final Class<?> pageClass = value.getPageClass();
        final Object[] activationContext = value.getPageActivationContext();

        final String pageName = this.resolver.resolvePageClassNameToPageName(pageClass.getName());

        final EventContext context = activationContext == null ? new EmptyEventContext() : new ArrayEventContext(
                this.typeCoercer, activationContext);

        this.handler.handle(new PageRenderRequestParameters(pageName, context, false));
    }
}
