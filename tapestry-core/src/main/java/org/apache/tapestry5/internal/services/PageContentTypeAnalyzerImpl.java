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
import org.apache.tapestry5.SymbolConstants;
import org.apache.tapestry5.internal.structure.Page;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.ioc.annotations.Symbol;
import org.apache.tapestry5.services.MetaDataLocator;

public class PageContentTypeAnalyzerImpl implements PageContentTypeAnalyzer
{
    private final MetaDataLocator metaDataLocator;

    private final String outputCharset;

    public PageContentTypeAnalyzerImpl(MetaDataLocator metaDataLocator,

                                       @Inject @Symbol(SymbolConstants.CHARSET)
                                       String outputCharset)
    {
        this.metaDataLocator = metaDataLocator;
        this.outputCharset = outputCharset;
    }

    public ContentType findContentType(Page page)
    {
        ComponentResources pageResources = page.getRootComponent().getComponentResources();

        String contentTypeString = metaDataLocator.findMeta(MetaDataConstants.RESPONSE_CONTENT_TYPE, pageResources,
                                                            String.class);

        // Draconian but necessary: overwrite the content type they selected with the application-wide output charset.

        return new ContentType(contentTypeString, outputCharset);
    }
}
