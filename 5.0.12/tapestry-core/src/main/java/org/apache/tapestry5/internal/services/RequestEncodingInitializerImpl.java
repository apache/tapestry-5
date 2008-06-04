// Copyright 2007, 2008 The Apache Software Foundation
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

import org.apache.tapestry5.ComponentResources;
import org.apache.tapestry5.ContentType;
import org.apache.tapestry5.MetaDataConstants;
import org.apache.tapestry5.internal.structure.Page;
import org.apache.tapestry5.services.MetaDataLocator;
import org.apache.tapestry5.services.Request;

public class RequestEncodingInitializerImpl implements RequestEncodingInitializer
{
    private final RequestPageCache cache;

    private final MetaDataLocator locator;

    private final Request request;

    public RequestEncodingInitializerImpl(RequestPageCache cache, MetaDataLocator locator, Request request)
    {
        this.cache = cache;
        this.locator = locator;
        this.request = request;
    }

    public void initializeRequestEncoding(String pageName)
    {
        Page page = cache.get(pageName);
        ComponentResources pageResources = page.getRootElement().getComponentResources();

        String contentTypeString = locator.findMeta(MetaDataConstants.RESPONSE_CONTENT_TYPE, pageResources,
                                                    String.class);
        ContentType contentType = new ContentType(contentTypeString);

        String encoding = contentType.getParameter("charset");

        if (encoding == null)
            encoding = locator.findMeta(MetaDataConstants.RESPONSE_ENCODING, pageResources, String.class);

        request.setEncoding(encoding);
    }

}
