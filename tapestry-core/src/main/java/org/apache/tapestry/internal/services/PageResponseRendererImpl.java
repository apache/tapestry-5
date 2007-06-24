// Copyright 2006, 2007 The Apache Software Foundation
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

import java.io.IOException;
import java.io.PrintWriter;

import org.apache.tapestry.ComponentResources;
import org.apache.tapestry.MarkupWriter;
import org.apache.tapestry.TapestryConstants;
import org.apache.tapestry.internal.structure.Page;
import org.apache.tapestry.internal.util.ContentType;
import org.apache.tapestry.services.MarkupWriterFactory;
import org.apache.tapestry.services.MetaDataLocator;
import org.apache.tapestry.services.Response;

public class PageResponseRendererImpl implements PageResponseRenderer
{
    public static final String CHARSET = "charset";

    private final PageMarkupRenderer _markupRenderer;

    private final MarkupWriterFactory _markupWriterFactory;

    private final MetaDataLocator _metaDataLocator;

    public PageResponseRendererImpl(MarkupWriterFactory markupWriterFactory,
            PageMarkupRenderer markupRenderer, MetaDataLocator metaDataLocator)
    {
        _markupWriterFactory = markupWriterFactory;
        _markupRenderer = markupRenderer;
        _metaDataLocator = metaDataLocator;
    }

    public void renderPageResponse(Page page, Response response) throws IOException
    {
        ComponentResources pageResources = page.getRootComponent().getComponentResources();

        String contentTypeString = _metaDataLocator.findMeta(
                TapestryConstants.RESPONSE_CONTENT_TYPE,
                pageResources);
        ContentType contentType = new ContentType(contentTypeString);

        // Make sure thre's always a charset specified.
        
        String encoding = contentType.getParameter(CHARSET);
        if (encoding == null)
        {
            encoding = _metaDataLocator
                    .findMeta(TapestryConstants.RESPONSE_ENCODING, pageResources);
            contentType.setParameter(CHARSET, encoding);
        }

        // Eventually we'll have to do work to figure out the correct markup type, content type,
        // whatever. Right now its defaulting to plain HTML.

        MarkupWriter writer = _markupWriterFactory.newMarkupWriter();

        _markupRenderer.renderPageMarkup(page, writer);

        PrintWriter pw = response.getPrintWriter(contentType.toString());

        writer.toMarkup(pw);

        pw.flush();
    }

}
