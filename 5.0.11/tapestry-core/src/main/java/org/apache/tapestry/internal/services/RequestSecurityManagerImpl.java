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

package org.apache.tapestry.internal.services;

import org.apache.tapestry.Link;
import org.apache.tapestry.TapestryConstants;
import org.apache.tapestry.internal.structure.Page;
import org.apache.tapestry.services.BaseURLSource;
import org.apache.tapestry.services.MetaDataLocator;
import org.apache.tapestry.services.Request;
import org.apache.tapestry.services.Response;

import java.io.IOException;

public class RequestSecurityManagerImpl implements RequestSecurityManager
{
    private final Request _request;

    private final Response _response;

    private final LinkFactory _linkFactory;

    private final MetaDataLocator _locator;

    private final BaseURLSource _baseURLSource;

    private final RequestPageCache _requestPageCache;

    public RequestSecurityManagerImpl(Request request, Response response, LinkFactory linkFactory,
                                      MetaDataLocator locator, BaseURLSource baseURLSource,
                                      RequestPageCache requestPageCache)
    {
        _request = request;
        _response = response;
        _linkFactory = linkFactory;
        _locator = locator;
        _baseURLSource = baseURLSource;
        _requestPageCache = requestPageCache;
    }

    public boolean checkForInsecureRequest(String pageName) throws IOException
    {
        // We don't (at this time) redirect from secure to insecure, just form insecure to secure.

        if (_request.isSecure()) return false;

        Page page = _requestPageCache.get(pageName);

        if (!isSecure(page)) return false;

        // Page is secure but request is not, so redirect.

        Link link = _linkFactory.createPageLink(page, false);

        _response.sendRedirect(link);

        return true;
    }

    private boolean isSecure(Page page)
    {
        return _locator.findMeta(TapestryConstants.SECURE_PAGE,
                                 page.getRootComponent().getComponentResources(), Boolean.class);
    }

    public String getBaseURL(Page page)
    {
        boolean securePage = isSecure(page);

        if (securePage == _request.isSecure()) return null;

        return _baseURLSource.getBaseURL(securePage);
    }
}
