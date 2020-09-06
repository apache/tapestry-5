// Copyright 2010 The Apache Software Foundation
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

package org.apache.tapestry5.internal.services.linktransform;

import org.apache.tapestry5.http.Link;
import org.apache.tapestry5.http.services.Request;
import org.apache.tapestry5.services.ComponentEventLinkEncoder;
import org.apache.tapestry5.services.ComponentEventRequestParameters;
import org.apache.tapestry5.services.PageRenderRequestParameters;
import org.apache.tapestry5.services.linktransform.LinkTransformer;

/**
 * Intercepts some methods of {@link ComponentEventLinkEncoder}, passing the returned {@link Link}s
 * through methods of{@link LinkTransformer}.
 * 
 * @since 5.2.0
 */
public class LinkTransformerInterceptor implements ComponentEventLinkEncoder
{
    private final LinkTransformer linkTransformer;

    private final ComponentEventLinkEncoder delegate;

    public LinkTransformerInterceptor(LinkTransformer linkTransformer, ComponentEventLinkEncoder delegate)
    {
        this.linkTransformer = linkTransformer;
        this.delegate = delegate;
    }

    public Link createComponentEventLink(ComponentEventRequestParameters parameters, boolean forForm)
    {
        return linkTransformer.transformComponentEventLink(delegate.createComponentEventLink(parameters, forForm),
                parameters);
    }

    public Link createPageRenderLink(PageRenderRequestParameters parameters)
    {
        return linkTransformer.transformPageRenderLink(delegate.createPageRenderLink(parameters), parameters);
    }

    public ComponentEventRequestParameters decodeComponentEventRequest(Request request)
    {
        ComponentEventRequestParameters parameters = linkTransformer.decodeComponentEventRequest(request);

        if (parameters == null)
            parameters = delegate.decodeComponentEventRequest(request);

        return parameters;
    }

    public PageRenderRequestParameters decodePageRenderRequest(Request request)
    {
        PageRenderRequestParameters parameters = linkTransformer.decodePageRenderRequest(request);

        if (parameters == null)
            parameters = delegate.decodePageRenderRequest(request);

        return parameters;
    }

}
