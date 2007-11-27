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
import org.apache.tapestry.internal.util.ContentType;
import org.apache.tapestry.json.JSONObject;
import org.apache.tapestry.runtime.RenderCommand;
import org.apache.tapestry.services.Environment;
import org.apache.tapestry.services.MarkupWriterFactory;
import org.apache.tapestry.services.Response;

import java.io.IOException;
import java.io.PrintWriter;

public class PartialMarkupRendererImpl implements PartialMarkupRenderer
{
    private final Environment _environment;

    private final PageRenderQueue _pageRenderQueue;

    private final MarkupWriterFactory _factory;

    private final Response _response;

    public PartialMarkupRendererImpl(Environment environment, PageRenderQueue pageRenderQueue,
                                     MarkupWriterFactory factory, Response response)
    {
        _environment = environment;
        _pageRenderQueue = pageRenderQueue;
        _factory = factory;
        _response = response;
    }

    public void renderPartialPageMarkup(RenderCommand rootRenderCommand) throws IOException
    {
        _environment.clear();

        // This may be problematic as the charset of the response is not
        // going to be set properly I think.  We'll loop back to that.

        ContentType contentType = new ContentType("text/javascript");

        MarkupWriter writer = _factory.newMarkupWriter();

        // The partial will quite often contain multiple elements (or just a block of plain text),
        // so those must be enclosed in a root element.

        Element root = writer.element("ajax-partial");

        _pageRenderQueue.initializeForPartialPageRender(rootRenderCommand);

        // TODO: This is where we will set up a pipeline to provide environmentals and,
        // perhaps, to catch errors and inform the client.

        _pageRenderQueue.render(writer);

        writer.end();

        String content = root.getChildMarkup().trim();

        JSONObject reply = new JSONObject();

        reply.put("content", content);

        PrintWriter pw = _response.getPrintWriter(contentType.toString());

        pw.print(reply);

        pw.flush();
    }
}
