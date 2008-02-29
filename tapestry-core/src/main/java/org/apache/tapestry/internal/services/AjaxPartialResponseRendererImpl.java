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

package org.apache.tapestry.internal.services;

import org.apache.tapestry.ContentType;
import org.apache.tapestry.MarkupWriter;
import org.apache.tapestry.internal.InternalConstants;
import org.apache.tapestry.json.JSONObject;
import org.apache.tapestry.services.MarkupWriterFactory;
import org.apache.tapestry.services.PartialMarkupRenderer;
import org.apache.tapestry.services.Request;
import org.apache.tapestry.services.Response;

import java.io.IOException;
import java.io.PrintWriter;

public class AjaxPartialResponseRendererImpl implements AjaxPartialResponseRenderer
{
    private final MarkupWriterFactory _factory;

    private final Request _request;

    private final Response _response;

    private final PartialMarkupRenderer _partialMarkupRenderer;

    public AjaxPartialResponseRendererImpl(MarkupWriterFactory factory, Request request,
                                           Response response, PartialMarkupRenderer partialMarkupRenderer)
    {
        _factory = factory;
        _request = request;
        _response = response;
        _partialMarkupRenderer = partialMarkupRenderer;
    }

    public void renderPartialPageMarkup() throws IOException
    {
        // This is a complex area as we are trying to keep public and private services properly
        // seperated, and trying to keep stateless and stateful (i.e., perthread scope) services
        // seperated. So we inform the stateful queue service what it needs to do here ...

        ContentType pageContentType = (ContentType) _request.getAttribute(
                InternalConstants.CONTENT_TYPE_ATTRIBUTE_NAME);
        String charset = pageContentType.getParameter(InternalConstants.CHARSET_CONTENT_TYPE_PARAMETER);

        ContentType contentType = new ContentType(InternalConstants.JSON_MIME_TYPE);
        contentType.setParameter(InternalConstants.CHARSET_CONTENT_TYPE_PARAMETER, charset);

        MarkupWriter writer = _factory.newMarkupWriter(pageContentType);

        JSONObject reply = new JSONObject();

        // ... and here, the pipeline eventually reaches the PRQ to let it render the root render command.

        _partialMarkupRenderer.renderMarkup(writer, reply);

        PrintWriter pw = _response.getPrintWriter(contentType.toString());

        pw.print(reply);

        pw.flush();
    }
}
