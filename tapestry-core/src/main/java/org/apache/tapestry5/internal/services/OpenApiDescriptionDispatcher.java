// Copyright 2021 The Apache Software Foundation
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
import java.nio.charset.Charset;

import org.apache.tapestry5.SymbolConstants;
import org.apache.tapestry5.http.services.Dispatcher;
import org.apache.tapestry5.http.services.Request;
import org.apache.tapestry5.http.services.Response;
import org.apache.tapestry5.ioc.annotations.Symbol;
import org.apache.tapestry5.json.JSONObject;
import org.apache.tapestry5.services.rest.OpenApiDescriptionGenerator;

/**
 * Recognizes requests where the path matches the value of {@link SymbolConstants#OPENAPI_DESCRIPTION_PATH}
 * (<code>/openapi.json</code> by default). Only used if {@link SymbolConstants#PUBLISH_OPENAPI_DEFINITON}
 * is set to <code>true</code> (which isn't by default).
 * @see OpenApiDescriptionGenerator
 */
public class OpenApiDescriptionDispatcher implements Dispatcher
{
    
    private final String path;
    private final OpenApiDescriptionGenerator openApiDescriptionGenerator;
    private byte[] cachedDescription;
    private final boolean productionMode;
    
    public OpenApiDescriptionDispatcher(@Symbol(SymbolConstants.OPENAPI_DESCRIPTION_PATH) final String path,
            final OpenApiDescriptionGenerator openApiDescriptionGenerator,
            final @Symbol(SymbolConstants.PRODUCTION_MODE) boolean productionMode)
    {
        this.path = path;
        this.openApiDescriptionGenerator = openApiDescriptionGenerator;
        this.productionMode = productionMode;
    }
    
    public boolean dispatch(Request request, Response response) throws IOException
    {
        boolean dispatched = false;
        if (path.equals(request.getPath()))
        {
            final byte[] description = getDescriptionAsByteArray();
            response.setContentLength(description.length);
            response.getOutputStream("application/json").write(description);
            dispatched = true;
        }
        return dispatched;
    }

    private byte[] getDescriptionAsByteArray() 
    {
        byte[] bytes;
        if (productionMode && cachedDescription != null)
        {
            bytes = cachedDescription;
        }
        else
        {
            bytes = openApiDescriptionGenerator
                    .generate(new JSONObject())
                    .toCompactString()
                    .getBytes(Charset.forName("UTF-8"));
            if (productionMode)
            {
                cachedDescription = bytes;
            }
        }
        return bytes;
    }
}
