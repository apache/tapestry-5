// Copyright 2021 The Apache Software Foundation
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

import java.io.IOException;
import java.util.Map;
import java.util.Objects;

import org.apache.tapestry5.http.services.Response;
import org.apache.tapestry5.services.ComponentEventResultProcessor;
import org.apache.tapestry5.services.HttpStatus;

/**
 * Handles {@link HttpStatus}.values returned by event handler methods.
 *
 * @since 5.8.0
 */
public class HttpStatusComponentEventResultProcessor implements ComponentEventResultProcessor<HttpStatus>
{
    private final Response response;

    public HttpStatusComponentEventResultProcessor(Response response)
    {
        this.response = response;
    }

    public void processResultValue(HttpStatus value) throws IOException
    {
        response.setStatus(value.getStatusCode());
        final Map<String, String> extraHttpHeaders = value.getExtraHttpHeaders();
        for (String header : extraHttpHeaders.keySet())
        {
            final String headerValue = extraHttpHeaders.get(header);
            response.setHeader(header, headerValue);
        }
        if (value.getResponseBody() != null)
        {
            Objects.requireNonNull(value.getContentType(), "HttpStatus.mimeType cannot be null");
            response.getPrintWriter(value.getContentType()).append(value.getResponseBody()).close();
        }
    }
}
