// Copyright 2011 The Apache Software Foundation
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

import org.apache.tapestry5.http.services.Response;
import org.apache.tapestry5.services.ComponentEventResultProcessor;
import org.apache.tapestry5.services.HttpError;

import java.io.IOException;

/**
 * Handles {@link HttpError} by invoking {@link Response#sendError(int, String)}.
 *
 * @since 5.3
 */
public class HttpErrorComponentEventResultProcessor implements ComponentEventResultProcessor<HttpError>
{
    private final Response response;

    public HttpErrorComponentEventResultProcessor(Response response)
    {
        this.response = response;
    }

    public void processResultValue(HttpError value) throws IOException
    {
        response.sendError(value.getStatusCode(), value.getMessage());
    }
}
