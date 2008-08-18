// Copyright 2008 The Apache Software Foundation
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
import org.apache.tapestry5.internal.structure.Page;
import org.apache.tapestry5.services.BaseURLSource;
import org.apache.tapestry5.services.MetaDataLocator;
import org.apache.tapestry5.services.Request;
import org.apache.tapestry5.services.Response;

import java.io.IOException;

public class RequestSecurityManagerImpl implements RequestSecurityManager
{
    private final Request request;

    private final Response response;

    private final LinkFactory linkFactory;

    private final MetaDataLocator locator;

    private final BaseURLSource baseURLSource;

    private final RequestPageCache requestPageCache;

    public RequestSecurityManagerImpl(Request request, Response response, LinkFactory linkFactory,
                                      MetaDataLocator locator, BaseURLSource baseURLSource,
                                      RequestPageCache requestPageCache)
    {
        this.request = request;
        this.response = response;
        this.linkFactory = linkFactory;
        this.locator = locator;
        this.baseURLSource = baseURLSource;
        this.requestPageCache = requestPageCache;
    }

    public boolean checkForInsecureRequest(String pageName) throws IOException
    {
        // We don't (at this time) redirect from secure to insecure, just form insecure to secure.

        if (request.isSecure()) return false;

        Page page = requestPageCache.get(pageName);

        if (!isSecure(page)) return false;

        // Page is secure but request is not, so redirect.

        Link link = linkFactory.createPageRenderLink(page, false);

        response.sendRedirect(link);

        return true;
    }

    private boolean isSecure(Page page)
    {
        return locator.findMeta(MetaDataConstants.SECURE_PAGE,
                                page.getRootComponent().getComponentResources(), Boolean.class);
    }

    public String getBaseURL(Page page)
    {
        boolean securePage = isSecure(page);

        if (securePage == request.isSecure()) return null;

        return baseURLSource.getBaseURL(securePage);
    }
}
