// Copyright 2009 The Apache Software Foundation
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

import javax.servlet.http.HttpServletRequest;

import org.apache.tapestry5.Link;
import org.apache.tapestry5.ioc.Invocation;
import org.apache.tapestry5.ioc.MethodAdvice;
import org.apache.tapestry5.ioc.internal.util.Defense;
import org.apache.tapestry5.services.ComponentEventLinkEncoder;
import org.apache.tapestry5.services.Request;
import org.apache.tapestry5.services.Response;
import org.apache.tapestry5.urlrewriter.SimpleRequestWrapper;

/**
 * Advices
 * {@linkplain ComponentEventLinkEncoder#createComponentEventLink(org.apache.tapestry5.services.ComponentEventRequestParameters, boolean)}
 * and
 * {@linkplain ComponentEventLinkEncoder#createPageRenderLink(org.apache.tapestry5.services.PageRenderRequestParameters)}
 * to rewrites the returned links using {@linkplain URLRewriterService}.
 */
public class ComponentEventLinkEncoderMethodAdvice implements MethodAdvice
{

    final private URLRewriterService urlRewriterService;

    final private Request request;

    final private HttpServletRequest httpServletRequest;

    final private Response response;

    /**
     * Single constructor of this class.
     * 
     * @param urlRewriterService
     *            an {@link URLRewriterService}. It cannot be null.
     * @param request
     *            a {@link Request}. It cannot be null.
     * @param httpServletRequest
     *            an {@link HttpServletRequest}. It cannot be null.
     * @param response
     *            a {@link Response}. It cannot be null.
     */
    public ComponentEventLinkEncoderMethodAdvice(URLRewriterService urlRewriterService,
            Request request, HttpServletRequest httpServletRequest, Response response)
    {

        Defense.notNull(urlRewriterService, "urlRewriterService");
        Defense.notNull(request, "request");
        Defense.notNull(httpServletRequest, "httpServletRequest");
        Defense.notNull(response, "response");

        this.httpServletRequest = httpServletRequest;
        this.urlRewriterService = urlRewriterService;
        this.request = request;
        this.response = response;

    }

    public void advise(Invocation invocation)
    {
        invocation.proceed();

        Link link = (Link) invocation.getResult();

        Link newLink = rewriteIfNeeded(link);

        if (newLink != null)
        {
            invocation.overrideResult(newLink);
        }

    }

    /**
     * Returns a rewritten Link or null.
     * 
     * @param link
     *            a {@link Link}.
     */
    Link rewriteIfNeeded(Link link)
    {
        Link newLink = null;
        SimpleRequestWrapper fakeRequest = new SimpleRequestWrapper(request, link.toAbsoluteURI());

        Request rewritten = urlRewriterService.process(fakeRequest);

        // if the original request is equal to the rewritten one, no
        // rewriting is needed
        if (fakeRequest != rewritten)
        {

            final String originalServerName = request.getServerName();
            final String rewrittenServerName = rewritten.getServerName();
            boolean absolute = originalServerName.equals(rewrittenServerName) == false;
            final String newPath = rewritten.getPath();

            String newUrl = absolute ? fullUrl(rewritten) : newPath;

            newLink = new LinkImpl(newUrl, false, false, response, null);

        }

        return newLink;

    }

    String fullUrl(Request request)
    {

        String protocol = request.isSecure() ? "https://" : "http://";
        final int localPort = httpServletRequest.getLocalPort();
        String port = localPort == 80 ? "" : ":" + localPort;

        final String path = request.getPath();
        final String contextPath = request.getContextPath();
        return String.format("%s%s%s%s%s", protocol, request.getServerName(), port, contextPath, path);

    }

}
