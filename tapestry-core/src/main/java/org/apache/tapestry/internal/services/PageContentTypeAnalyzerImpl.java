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
import org.apache.tapestry.ContentType;
import org.apache.tapestry.TapestryConstants;
import org.apache.tapestry.internal.InternalConstants;
import org.apache.tapestry.internal.structure.Page;
import org.apache.tapestry.services.MetaDataLocator;

public class PageContentTypeAnalyzerImpl implements PageContentTypeAnalyzer
{
    private final MetaDataLocator _metaDataLocator;

    public PageContentTypeAnalyzerImpl(MetaDataLocator metaDataLocator)
    {
        _metaDataLocator = metaDataLocator;
    }

    public ContentType findContentType(Page page)
    {
        ComponentResources pageResources = page.getRootComponent().getComponentResources();

        String contentTypeString = _metaDataLocator.findMeta(TapestryConstants.RESPONSE_CONTENT_TYPE, pageResources);
        ContentType contentType = new ContentType(contentTypeString);

        // Make sure thre's always a charset specified.

        String encoding = contentType.getParameter(InternalConstants.CHARSET_CONTENT_TYPE_PARAMETER);

        if (encoding == null)
        {
            encoding = _metaDataLocator
                    .findMeta(TapestryConstants.RESPONSE_ENCODING, pageResources);
            contentType.setParameter(InternalConstants.CHARSET_CONTENT_TYPE_PARAMETER, encoding);
        }

        return contentType;
    }
}
