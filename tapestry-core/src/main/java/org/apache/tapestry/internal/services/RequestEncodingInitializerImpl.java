// Copyright 2007 The Apache Software Foundation
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

import org.apache.tapestry.ComponentResources;
import org.apache.tapestry.TapestryConstants;
import org.apache.tapestry.internal.structure.Page;
import org.apache.tapestry.internal.util.ContentType;
import org.apache.tapestry.services.MetaDataLocator;
import org.apache.tapestry.services.Request;

public class RequestEncodingInitializerImpl implements RequestEncodingInitializer
{
    private final RequestPageCache _cache;

    private final MetaDataLocator _locator;

    private final Request _request;

    public RequestEncodingInitializerImpl(RequestPageCache cache, MetaDataLocator locator,
                                          Request request)
    {
        _cache = cache;
        _locator = locator;
        _request = request;
    }

    public void initializeRequestEncoding(String pageName)
    {
        Page page = _cache.get(pageName);
        ComponentResources pageResources = page.getRootElement().getComponentResources();

        String contentTypeString = _locator.findMeta(
                TapestryConstants.RESPONSE_CONTENT_TYPE,
                pageResources);
        ContentType contentType = new ContentType(contentTypeString);

        String encoding = contentType.getParameter("charset");

        if (encoding == null)
            encoding = _locator.findMeta(TapestryConstants.RESPONSE_ENCODING, pageResources);

        _request.setEncoding(encoding);
    }

}
