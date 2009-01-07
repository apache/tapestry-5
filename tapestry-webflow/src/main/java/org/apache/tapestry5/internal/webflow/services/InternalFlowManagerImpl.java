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

import org.apache.tapestry5.ioc.annotations.Primary;
import org.apache.tapestry5.services.ComponentEventResultProcessor;
import org.apache.tapestry5.services.Response;
import org.apache.tapestry5.services.Traditional;
import org.springframework.webflow.context.servlet.FlowUrlHandler;
import org.springframework.webflow.executor.FlowExecutionResult;
import org.springframework.webflow.executor.FlowExecutor;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

public class InternalFlowManagerImpl implements InternalFlowManager
{
    private final FlowExecutor flowExecutor;

    private final ExternalContextSource contextSource;

    private final FlowUrlHandler urlHandler;

    private final HttpServletRequest request;

    private final ComponentEventResultProcessor resultProcessor;

    private final Response response;

    public InternalFlowManagerImpl(FlowExecutor flowExecutor,

                                   ExternalContextSource contextSource,

                                   FlowUrlHandler urlHandler,

                                   HttpServletRequest request,

                                   @Traditional @Primary
                                   ComponentEventResultProcessor resultProcessor,

                                   Response response)
    {
        this.flowExecutor = flowExecutor;
        this.contextSource = contextSource;
        this.urlHandler = urlHandler;
        this.request = request;
        this.resultProcessor = resultProcessor;
        this.response = response;
    }

    public void continueFlow() throws IOException
    {
        String executionKey = urlHandler.getFlowExecutionKey(request);

        FlowExecutionResult result = flowExecutor.resumeExecution(executionKey, contextSource.create());

        // Typically, the response will be committed (a redirect to a render request) by now,
        // but just in case.

        if (!response.isCommitted())
            resultProcessor.processResultValue(result);
    }
}
