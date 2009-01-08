// Copyright 2006, 2007, 2008, 2009 The Apache Software Foundation
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
import org.slf4j.Logger;

import java.io.IOException;
import java.io.PrintWriter;

public class PageResponseRendererImpl implements PageResponseRenderer
{
    private final PageMarkupRenderer markupRenderer;

    private final MarkupWriterFactory markupWriterFactory;

    private final PageContentTypeAnalyzer pageContentTypeAnalyzer;

    private final Response response;

    private final Logger logger;

    public PageResponseRendererImpl(MarkupWriterFactory markupWriterFactory, PageMarkupRenderer markupRenderer,
                                    PageContentTypeAnalyzer pageContentTypeAnalyzer, Response response, Logger logger)
    {
        this.markupWriterFactory = markupWriterFactory;
        this.markupRenderer = markupRenderer;
        this.pageContentTypeAnalyzer = pageContentTypeAnalyzer;
        this.response = response;
        this.logger = logger;
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

        long startNanos = System.nanoTime();

        writer.toMarkup(pw);

        long endNanos = System.nanoTime();

        if (logger.isDebugEnabled())
        {
            long elapsedNanos = endNanos - startNanos;
            double elapsedSeconds = ((float) elapsedNanos) / 1000000000F;

            logger.debug(String.format("Response DOM streamed to markup in %.3f seconds",
                                       elapsedSeconds));
        }

        pw.close();
    }
}
