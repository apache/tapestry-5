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

import org.apache.tapestry.MarkupWriter;
import org.apache.tapestry.dom.Element;
import org.apache.tapestry.internal.structure.Page;
import org.apache.tapestry.internal.util.ContentType;
import org.apache.tapestry.json.JSONObject;
import org.apache.tapestry.runtime.RenderCommand;
import org.apache.tapestry.services.MarkupWriterFactory;
import org.apache.tapestry.services.Response;

import java.io.IOException;
import java.io.PrintWriter;

public class AjaxResponseGenerator
{
    private final Page _page;

    private final RenderCommand _rootRenderCommand;

    private final MarkupWriterFactory _markupWriterFactory;

    private final PageMarkupRenderer _renderer;


    public AjaxResponseGenerator(Page page, RenderCommand rootRenderCommand, MarkupWriterFactory markupWriterFactory,
                                 PageMarkupRenderer renderer)
    {
        _page = page;
        _rootRenderCommand = rootRenderCommand;
        _markupWriterFactory = markupWriterFactory;
        _renderer = renderer;
    }

    public void sendClientResponse(Response response) throws IOException
    {
        // This may be problematic as the charset of the response is not
        // going to be set properly I think.  We'll loop back to that.

        ContentType contentType = new ContentType("text/javascript");

        MarkupWriter writer = _markupWriterFactory.newMarkupWriter();

        // The partial will quite often contain multiple elements, so those must be enclosed in a root element.

        Element root = writer.element("ajax-partial");

        _renderer.renderPartialPageMarkup(_page, _rootRenderCommand, writer);

        writer.end();

        String content = root.getChildText().trim();

        JSONObject reply = new JSONObject();

        reply.put("content", content);

        PrintWriter pw = response.getPrintWriter(contentType.toString());

        pw.print(reply);

        pw.flush();
    }
}
