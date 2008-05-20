// Copyright 2006, 2007, 2008 The Apache Software Foundation
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
import org.apache.tapestry5.internal.structure.Page;
import org.apache.tapestry5.ioc.internal.util.Defense;
import org.apache.tapestry5.services.MarkupWriterFactory;
import org.apache.tapestry5.services.Response;

import java.io.IOException;
import java.io.PrintWriter;

public class PageResponseRendererImpl implements PageResponseRenderer
{
    private final PageMarkupRenderer markupRenderer;

    private final MarkupWriterFactory markupWriterFactory;

    private final PageContentTypeAnalyzer pageContentTypeAnalyzer;

    private final Response response;

    public PageResponseRendererImpl(MarkupWriterFactory markupWriterFactory, PageMarkupRenderer markupRenderer,
                                    PageContentTypeAnalyzer pageContentTypeAnalyzer, Response response)
    {
        this.markupWriterFactory = markupWriterFactory;
        this.markupRenderer = markupRenderer;
        this.pageContentTypeAnalyzer = pageContentTypeAnalyzer;
        this.response = response;
    }

    public void renderPageResponse(Page page) throws IOException
    {
        Defense.notNull(page, "page");

        ContentType contentType = pageContentTypeAnalyzer.findContentType(page);

        // For the moment, the content type is all that's used determine the model for the markup writer.
        // It's something of a can of worms.

        MarkupWriter writer = markupWriterFactory.newMarkupWriter(contentType);

        markupRenderer.renderPageMarkup(page, writer);

        PrintWriter pw = response.getPrintWriter(contentType.toString());

        writer.toMarkup(pw);

        pw.flush();
    }
}
