// Copyright 2007, 2008, 2009, 2010, 2011 The Apache Software Foundation
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

import org.apache.tapestry5.ContentType;
import org.apache.tapestry5.MarkupWriter;
import org.apache.tapestry5.SymbolConstants;
import org.apache.tapestry5.internal.InternalConstants;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.ioc.annotations.Symbol;
import org.apache.tapestry5.json.JSONObject;
import org.apache.tapestry5.services.*;

import java.io.IOException;
import java.io.PrintWriter;

public class AjaxPartialResponseRendererImpl implements AjaxPartialResponseRenderer
{
    private final MarkupWriterFactory factory;

    private final Request request;

    private final Response response;

    private final PartialMarkupRenderer partialMarkupRenderer;

    private final String outputEncoding;

    private final boolean compactJSON;

    private final Environment environment;

    public AjaxPartialResponseRendererImpl(MarkupWriterFactory factory,

                                           Request request,

                                           Response response,

                                           PartialMarkupRenderer partialMarkupRenderer,

                                           @Inject
                                           @Symbol(SymbolConstants.CHARSET)
                                           String outputEncoding,

                                           @Symbol(SymbolConstants.COMPACT_JSON)
                                           boolean compactJSON, Environment environment)
    {
        this.factory = factory;
        this.request = request;
        this.response = response;
        this.partialMarkupRenderer = partialMarkupRenderer;
        this.outputEncoding = outputEncoding;
        this.compactJSON = compactJSON;
        this.environment = environment;
    }

    public void renderPartialPageMarkup() throws IOException
    {
        environment.cloak();

        try
        {
            // This is a complex area as we are trying to keep public and private services properly
            // separated, and trying to keep stateless and stateful (i.e., perthread scope) services
            // separated. So we inform the stateful queue service what it needs to do here ...

            ContentType pageContentType = (ContentType) request.getAttribute(InternalConstants.CONTENT_TYPE_ATTRIBUTE_NAME);

            ContentType contentType = new ContentType(InternalConstants.JSON_MIME_TYPE, outputEncoding);

            MarkupWriter writer = factory.newPartialMarkupWriter(pageContentType);

            JSONObject reply = new JSONObject();

            // ... and here, the pipeline eventually reaches the PRQ to let it render the root render command.

            partialMarkupRenderer.renderMarkup(writer, reply);

            PrintWriter pw = response.getPrintWriter(contentType.toString());

            reply.print(pw, compactJSON);

            pw.close();
        } finally
        {
            environment.decloak();
        }
    }
}
