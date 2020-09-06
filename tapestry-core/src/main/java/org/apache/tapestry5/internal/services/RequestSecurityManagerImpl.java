// Copyright 2008-2013 The Apache Software Foundation
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

import org.apache.tapestry5.MetaDataConstants;
import org.apache.tapestry5.SymbolConstants;
import org.apache.tapestry5.beanmodel.services.*;
import org.apache.tapestry5.http.Link;
import org.apache.tapestry5.http.LinkSecurity;
import org.apache.tapestry5.http.services.Request;
import org.apache.tapestry5.http.services.Response;
import org.apache.tapestry5.ioc.annotations.Symbol;
import org.apache.tapestry5.services.ComponentEventLinkEncoder;
import org.apache.tapestry5.services.ComponentEventRequestParameters;
import org.apache.tapestry5.services.MetaDataLocator;
import org.apache.tapestry5.services.PageRenderRequestParameters;

import java.io.IOException;

public class RequestSecurityManagerImpl implements RequestSecurityManager
{
    private final Request request;

    private final Response response;

    private final MetaDataLocator locator;

    private final boolean securityEnabled;

    private final ComponentEventLinkEncoder componentEventLinkEncoder;

    public RequestSecurityManagerImpl(Request request, Response response,
                                      ComponentEventLinkEncoder componentEventLinkEncoder, MetaDataLocator locator, @Symbol(SymbolConstants.SECURE_ENABLED)
    boolean securityEnabled)
    {
        this.request = request;
        this.response = response;
        this.componentEventLinkEncoder = componentEventLinkEncoder;
        this.locator = locator;
        this.securityEnabled = securityEnabled;
    }

    public boolean checkForInsecureComponentEventRequest(ComponentEventRequestParameters parameters) throws IOException
    {
        if (!needsRedirect(parameters.getActivePageName()))
        {
            return false;
        }

        // Page is secure but request is not, so redirect.
        // We can safely ignore the forForm parameter since secure form requests are always done from
        // an already secured page

        Link link = componentEventLinkEncoder.createComponentEventLink(parameters, false);

        response.sendRedirect(link);

        return true;
    }

    public boolean checkForInsecurePageRenderRequest(PageRenderRequestParameters parameters) throws IOException
    {
        if (!needsRedirect(parameters.getLogicalPageName()))
            return false;

        // Page is secure but request is not, so redirect.

        Link link = componentEventLinkEncoder.createPageRenderLink(parameters);

        response.sendRedirect(link);

        return true;
    }

    private boolean needsRedirect(String pageName)
    {
        if (!securityEnabled)
        {
            return false;
        }

        // We don't (at this time) redirect from secure to insecure, just from insecure to secure.

        if (request.isSecure())
        {
            return false;
        }

        if (!isSecure(pageName))
        {
            return false;
        }

        return true;
    }

    private boolean isSecure(String pageName)
    {
        return locator.findMeta(MetaDataConstants.SECURE_PAGE, pageName, Boolean.class);
    }

    public LinkSecurity checkPageSecurity(String pageName)
    {
        if (!securityEnabled)
        {
            return request.isSecure() ? LinkSecurity.SECURE : LinkSecurity.INSECURE;
        }

        boolean securePage = isSecure(pageName);

        if (request.isSecure() == securePage)
        {
            return securePage ? LinkSecurity.SECURE : LinkSecurity.INSECURE;
        }

        // Return a value that will, ultimately, force an absolute URL.

        return securePage ? LinkSecurity.FORCE_SECURE : LinkSecurity.FORCE_INSECURE;
    }
}
