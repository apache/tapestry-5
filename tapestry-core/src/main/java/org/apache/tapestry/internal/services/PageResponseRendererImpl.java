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

import org.apache.tapestry.MarkupWriter;
import org.apache.tapestry.internal.structure.Page;
import org.apache.tapestry.services.MarkupWriterFactory;
import org.apache.tapestry.services.Response;

public class PageResponseRendererImpl implements PageResponseRenderer
{
    private final PageMarkupRenderer _markupRenderer;

    private final MarkupWriterFactory _markupWriterFactory;

    public PageResponseRendererImpl(MarkupWriterFactory markupWriterFactory,
            PageMarkupRenderer markupRenderer)
    {
        _markupWriterFactory = markupWriterFactory;
        _markupRenderer = markupRenderer;
    }

    public void renderPageResponse(Page page, Response response) throws IOException
    {
        // Eventually we'll have to do work to figure out the correct markup type, content type,
        // whatever. Right now its defaulting to plain HTML.

        MarkupWriter writer = _markupWriterFactory.newMarkupWriter();

        _markupRenderer.renderPageMarkup(page, writer);

        PrintWriter pw = response.getPrintWriter("text/html");

        writer.toMarkup(pw);

        pw.flush();
    }

}
