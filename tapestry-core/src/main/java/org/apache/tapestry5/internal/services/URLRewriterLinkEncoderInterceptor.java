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

package org.apache.tapestry5.internal.services;

import org.apache.tapestry5.Link;
import org.apache.tapestry5.services.ComponentEventLinkEncoder;
import org.apache.tapestry5.services.ComponentEventRequestParameters;
import org.apache.tapestry5.services.PageRenderRequestParameters;
import org.apache.tapestry5.services.Request;
import org.apache.tapestry5.services.Response;
import org.apache.tapestry5.services.URLRewriter;
import org.apache.tapestry5.urlrewriter.SimpleRequestWrapper;
import org.apache.tapestry5.urlrewriter.URLRewriteContext;

/**
 * An intercepter for the {@link ComponentEventLinkEncoder} service that is put into use when there
 * {@linkplain URLRewriter#hasLinkRules() URL link rewrite rules}.
 * 
 * @since 5.2.0
 */
public class URLRewriterLinkEncoderInterceptor implements ComponentEventLinkEncoder
{
    private final URLRewriter urlRewriter;

    private final Request request;

    private final Response response;

    private final ComponentEventLinkEncoder delegate;

    public URLRewriterLinkEncoderInterceptor(URLRewriter urlRewriter, Request request, Response response,
            ComponentEventLinkEncoder delegate)
    {
        this.urlRewriter = urlRewriter;
        this.request = request;
        this.response = response;
        this.delegate = delegate;
    }

    public Link createComponentEventLink(final ComponentEventRequestParameters parameters, boolean forForm)
    {
        Link standardLink = delegate.createComponentEventLink(parameters, forForm);

        URLRewriteContext rewriteContext = new URLRewriteContext()
        {
            public boolean isIncoming()
            {
                return false;
            }

            public PageRenderRequestParameters getPageParameters()
            {
                return null;
            }

            public ComponentEventRequestParameters getComponentEventParameters()
            {
                return parameters;
            }
        };

        return rewriteIfNeeded(standardLink, rewriteContext, forForm);
    }

    public Link createPageRenderLink(final PageRenderRequestParameters parameters)
    {
        Link standardLink = delegate.createPageRenderLink(parameters);

        URLRewriteContext rewriteContext = new URLRewriteContext()
        {
            public boolean isIncoming()
            {
                return false;
            }

            public PageRenderRequestParameters getPageParameters()
            {
                return parameters;
            }

            public ComponentEventRequestParameters getComponentEventParameters()
            {
                return null;
            }
        };

        return rewriteIfNeeded(standardLink, rewriteContext, false);
    }

    public ComponentEventRequestParameters decodeComponentEventRequest(Request request)
    {
        return delegate.decodeComponentEventRequest(request);
    }

    public PageRenderRequestParameters decodePageRenderRequest(Request request)
    {
        return delegate.decodePageRenderRequest(request);
    }

    private Link rewriteIfNeeded(Link link, URLRewriteContext context, boolean forForm)
    {
        SimpleRequestWrapper fakeRequest = new SimpleRequestWrapper(request, link.toAbsoluteURI());

        Request rewritten = urlRewriter.processLink(fakeRequest, context);

        // if the original request is equal to the rewritten one, no
        // rewriting is needed
        if (fakeRequest != rewritten)
        {
            String originalServerName = request.getServerName();

            String rewrittenServerName = rewritten.getServerName();

            boolean absolute = originalServerName.equals(rewrittenServerName) == false;

            String newPath = rewritten.getPath();

            String newUrl = absolute ? fullUrl(rewritten) : newPath;

            Link replacement = new LinkImpl(newUrl, false, forForm, response, null);

            copyParameters(link, replacement);

            return replacement;
        }

        return link;
    }

    private void copyParameters(Link link, Link replacement)
    {
        for (String name : link.getParameterNames())
        {
            replacement.addParameter(name, link.getParameterValue(name));
        }
    }

    private String fullUrl(Request request)
    {

        String protocol = request.isSecure() ? "https://" : "http://";

        int localPort = request.getLocalPort();

        String port = localPort == 80 ? "" : ":" + localPort;

        String path = request.getPath();
        String contextPath = request.getContextPath();

        return protocol + request.getServerName() + port + contextPath + path;
    }

}
