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

import org.apache.tapestry5.SymbolConstants;
import org.apache.tapestry5.http.ContentType;
import org.apache.tapestry5.http.TapestryHttpSymbolConstants;
import org.apache.tapestry5.http.services.Response;
import org.apache.tapestry5.internal.InternalConstants;
import org.apache.tapestry5.ioc.annotations.Symbol;
import org.apache.tapestry5.json.JSONArray;
import org.apache.tapestry5.services.ComponentEventResultProcessor;

import java.io.IOException;
import java.io.PrintWriter;

public class JSONArrayEventResultProcessor implements ComponentEventResultProcessor<JSONArray>
{
    private final Response response;

    private final boolean compactJSON;

    private final ContentType contentType;

    public JSONArrayEventResultProcessor(Response response,

                                         @Symbol(TapestryHttpSymbolConstants.CHARSET)
                                         String outputEncoding,

                                         @Symbol(SymbolConstants.COMPACT_JSON)
                                         boolean compactJSON)
    {
        this.response = response;
        this.compactJSON = compactJSON;

        contentType = new ContentType(InternalConstants.JSON_MIME_TYPE).withCharset(outputEncoding);
    }

    public void processResultValue(JSONArray value) throws IOException
    {
        PrintWriter pw = response.getPrintWriter(contentType.toString());

        value.print(pw, compactJSON);

        pw.close();
    }
}
