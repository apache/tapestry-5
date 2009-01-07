// Copyright 2009 The Apache Software Foundation
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

package org.apache.tapestry5.internal.webflow.services;

import org.apache.tapestry5.services.ComponentEventResultProcessor;
import org.apache.tapestry5.services.Response;
import org.springframework.webflow.context.servlet.FlowUrlHandler;
import org.springframework.webflow.executor.FlowExecutionResult;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

public class FlowExecutionResultProcessor implements ComponentEventResultProcessor<FlowExecutionResult>
{
    private final HttpServletRequest request;

    private final Response response;

    private final FlowUrlHandler urlHandler;

    public FlowExecutionResultProcessor(HttpServletRequest request, Response response, FlowUrlHandler urlHandler)
    {
        this.request = request;
        this.response = response;
        this.urlHandler = urlHandler;
    }

    public void processResultValue(FlowExecutionResult result) throws IOException
    {
        String url = urlHandler.createFlowExecutionUrl(result.getFlowId(), result.getPausedKey(), request);

        response.sendRedirect(response.encodeRedirectURL(url));
    }
}
