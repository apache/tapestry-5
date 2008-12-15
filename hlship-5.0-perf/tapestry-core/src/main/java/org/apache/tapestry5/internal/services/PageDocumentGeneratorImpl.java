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

package org.apache.tapestry5.internal.services;

import org.apache.tapestry5.MarkupWriter;
import org.apache.tapestry5.dom.Document;
import org.apache.tapestry5.internal.InternalConstants;
import org.apache.tapestry5.internal.structure.Page;
import org.apache.tapestry5.services.MarkupWriterFactory;
import org.apache.tapestry5.services.PageDocumentGenerator;
import org.apache.tapestry5.services.Request;

public class PageDocumentGeneratorImpl implements PageDocumentGenerator
{
    private final RequestPageCache pageCache;

    private final PageMarkupRenderer markupRenderer;

    private final MarkupWriterFactory markupWriterFactory;

    private final Request request;

    public PageDocumentGeneratorImpl(RequestPageCache pageCache, PageMarkupRenderer markupRenderer,
                                     MarkupWriterFactory markupWriterFactory,
                                     Request request)
    {
        this.markupRenderer = markupRenderer;
        this.markupWriterFactory = markupWriterFactory;
        this.pageCache = pageCache;
        this.request = request;
    }

    public Document render(String logicalPageName)
    {
        Page page = pageCache.get(logicalPageName);

        MarkupWriter writer = markupWriterFactory.newMarkupWriter(logicalPageName);

        // value will almost certainly be null, unless a page that is being rendered to a document
        // itself decides to render another page to a document.

        Object value = request.getAttribute(InternalConstants.GENERATING_RENDERED_PAGE);

        try
        {
            request.setAttribute(InternalConstants.GENERATING_RENDERED_PAGE, true);

            markupRenderer.renderPageMarkup(page, writer);
        }
        finally
        {
            request.setAttribute(InternalConstants.GENERATING_RENDERED_PAGE, value);
        }

        return writer.getDocument();
    }
}
