//  Copyright 2008 The Apache Software Foundation
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
import org.apache.tapestry5.SymbolConstants;
import org.apache.tapestry5.internal.InternalConstants;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.ioc.annotations.Symbol;
import org.apache.tapestry5.json.JSONArray;
import org.apache.tapestry5.services.ComponentEventResultProcessor;
import org.apache.tapestry5.services.Response;

import java.io.IOException;
import java.io.PrintWriter;

public class JSONArrayEventResultProcessor implements ComponentEventResultProcessor<JSONArray>
{
    private final Response response;
    private final String outputEncoding;

    public JSONArrayEventResultProcessor(Response response,

                                         @Inject @Symbol(SymbolConstants.CHARSET)
                                         String outputEncoding)
    {
        this.response = response;
        this.outputEncoding = outputEncoding;
    }

    public void processResultValue(JSONArray value) throws IOException
    {
        ContentType contentType = new ContentType(InternalConstants.JSON_MIME_TYPE, outputEncoding);

        PrintWriter pw = response.getPrintWriter(contentType.toString());

        pw.print(value.toString());

        pw.flush();
    }
}
