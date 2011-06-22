// Copyright 2007, 2008, 2009 The Apache Software Foundation
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

import org.apache.tapestry5.ContentType;
import org.apache.tapestry5.MarkupWriter;
import org.apache.tapestry5.dom.DefaultMarkupModel;
import org.apache.tapestry5.dom.MarkupModel;
import org.apache.tapestry5.dom.XMLMarkupModel;
import org.apache.tapestry5.internal.structure.Page;
import org.apache.tapestry5.services.MarkupWriterFactory;

public class MarkupWriterFactoryImpl implements MarkupWriterFactory
{
    private final PageContentTypeAnalyzer analyzer;

    private final RequestPageCache cache;

    private final MarkupModel htmlModel = new DefaultMarkupModel();

    private final MarkupModel xmlModel = new XMLMarkupModel();

    private final MarkupModel htmlPartialModel = new DefaultMarkupModel(true);

    private final MarkupModel xmlPartialModel = new XMLMarkupModel(true);

    public MarkupWriterFactoryImpl(PageContentTypeAnalyzer analyzer, RequestPageCache cache)
    {
        this.analyzer = analyzer;
        this.cache = cache;
    }

    public MarkupWriter newMarkupWriter(ContentType contentType)
    {
        return newMarkupWriter(contentType, false);
    }

    public MarkupWriter newPartialMarkupWriter(ContentType contentType)
    {
        return newMarkupWriter(contentType, true);
    }

    @SuppressWarnings({"UnusedDeclaration"})
    private MarkupWriter newMarkupWriter(ContentType contentType, boolean partial)
    {
        boolean isHTML = contentType.getMimeType().equalsIgnoreCase("text/html");

        MarkupModel model = partial
                            ? (isHTML ? htmlPartialModel : xmlPartialModel)
                            : (isHTML ? htmlModel : xmlModel);

        // The charset parameter sets the encoding attribute of the XML declaration, if
        // not null and if using the XML model.

        return new MarkupWriterImpl(model, contentType.getCharset());
    }

    public MarkupWriter newMarkupWriter(String pageName)
    {
        Page page = cache.get(pageName);

        ContentType contentType = analyzer.findContentType(page);

        return newMarkupWriter(contentType);
    }
}
