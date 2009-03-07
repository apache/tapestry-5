// Copyright 2008, 2009 The Apache Software Foundation
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

import org.apache.tapestry5.Link;
import org.apache.tapestry5.MetaDataConstants;
import org.apache.tapestry5.SymbolConstants;
import org.apache.tapestry5.ioc.annotations.Symbol;
import org.apache.tapestry5.services.BaseURLSource;
import org.apache.tapestry5.services.MetaDataLocator;
import org.apache.tapestry5.services.Request;
import org.apache.tapestry5.services.Response;

import java.io.IOException;

public class RequestSecurityManagerImpl implements RequestSecurityManager
{
    private final Request request;

    private final Response response;

    private final LinkSource linkSource;

    private final MetaDataLocator locator;

    private final BaseURLSource baseURLSource;

    private final boolean securityEnabled;

    public RequestSecurityManagerImpl(Request request, Response response, LinkSource linkSource,
                                      MetaDataLocator locator, BaseURLSource baseURLSource,

                                      @Symbol(SymbolConstants.SECURE_ENABLED)
                                      boolean securityEnabled)
    {
        this.request = request;
        this.response = response;
        this.linkSource = linkSource;
        this.locator = locator;
        this.baseURLSource = baseURLSource;
        this.securityEnabled = securityEnabled;
    }

    public boolean checkForInsecureRequest(String pageName) throws IOException
    {
        if (!securityEnabled) return false;

        // We don't (at this time) redirect from secure to insecure, just from insecure to secure.

        if (request.isSecure()) return false;

        if (!isSecure(pageName)) return false;

        // Page is secure but request is not, so redirect.

        Link link = linkSource.createPageRenderLink(pageName, false);

        response.sendRedirect(link);

        return true;
    }

    private boolean isSecure(String pageName)
    {
        return locator.findMeta(MetaDataConstants.SECURE_PAGE, pageName, Boolean.class);
    }

    public String getBaseURL(String pageName)
    {
        if (!securityEnabled) return null;

        boolean securePage = isSecure(pageName);

        if (securePage == request.isSecure()) return null;

        return baseURLSource.getBaseURL(securePage);
    }
}
