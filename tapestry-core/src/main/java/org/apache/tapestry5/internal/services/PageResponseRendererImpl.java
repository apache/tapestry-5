// Copyright 2006-2013 The Apache Software Foundation
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.apache.tapestry5.internal.services;

import java.io.IOException;
import java.io.PrintWriter;

import org.apache.tapestry5.MarkupWriter;
import org.apache.tapestry5.http.ContentType;
import org.apache.tapestry5.http.services.RequestGlobals;
import org.apache.tapestry5.http.services.Response;
import org.apache.tapestry5.internal.structure.Page;
import org.apache.tapestry5.services.MarkupWriterFactory;
import org.slf4j.Logger;

public class PageResponseRendererImpl implements PageResponseRenderer
{
    private final RequestGlobals requestGlobals;

    private final PageMarkupRenderer markupRenderer;

    private final MarkupWriterFactory markupWriterFactory;

    private final PageContentTypeAnalyzer pageContentTypeAnalyzer;

    private final Response response;

    private final Logger logger;

    public PageResponseRendererImpl(RequestGlobals requestGlobals, MarkupWriterFactory markupWriterFactory,
            PageMarkupRenderer markupRenderer, PageContentTypeAnalyzer pageContentTypeAnalyzer, Response response,
            Logger logger)
    {
        this.requestGlobals = requestGlobals;
        this.markupWriterFactory = markupWriterFactory;
        this.markupRenderer = markupRenderer;
        this.pageContentTypeAnalyzer = pageContentTypeAnalyzer;
        this.response = response;
        this.logger = logger;
    }

    public void renderPageResponse(Page page) throws IOException
    {
        assert page != null;

        requestGlobals.storeActivePageName(page.getName());

        ContentType contentType = pageContentTypeAnalyzer.findContentType(page);
        
        MarkupWriter writer = markupWriterFactory.newMarkupWriter(page);

        markupRenderer.renderPageMarkup(page, writer);

        PrintWriter pw = response.getPrintWriter(contentType.toString());
        long startNanos = -1l;
        boolean debugEnabled = logger.isDebugEnabled();
        if (debugEnabled)
        {
            startNanos = System.nanoTime();
        }
        writer.toMarkup(pw);


        if (debugEnabled)
        {
            long endNanos = System.nanoTime();
            long elapsedNanos = endNanos - startNanos;
            double elapsedSeconds = ((float) elapsedNanos) / 1000000000F;

            logger.debug(String.format("Response DOM streamed to markup in %.3f seconds", elapsedSeconds));
        }

        pw.close();
    }
}
