// Copyright 2009, 2011 Apache Software Foundation
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

import org.apache.tapestry5.beanmodel.services.*;
import org.apache.tapestry5.services.ComponentEventRequestHandler;
import org.apache.tapestry5.services.ComponentEventRequestParameters;
import org.apache.tapestry5.services.ComponentRequestHandler;
import org.apache.tapestry5.services.PageRenderRequestHandler;
import org.apache.tapestry5.services.PageRenderRequestParameters;
import org.apache.tapestry5.services.Traditional;

/**
 * Terminator for the {@link org.apache.tapestry5.services.ComponentRequestHandler} pipeline, that feeds out into the
 * {@link org.apache.tapestry5.services.ComponentEventRequestHandler} and {@link org.apache.tapestry5.services.PageRenderRequestHandler}
 * pipelines.
 *
 * @since 5.1.0.0
 */
public class ComponentRequestHandlerTerminator implements ComponentRequestHandler
{
    private final ComponentEventRequestHandler componentEventRequestHandler;

    private final PageRenderRequestHandler pageRenderRequestHandler;

    public ComponentRequestHandlerTerminator(@Traditional ComponentEventRequestHandler componentEventRequestHandler,
                                             PageRenderRequestHandler pageRenderRequestHandler)
    {
        this.componentEventRequestHandler = componentEventRequestHandler;
        this.pageRenderRequestHandler = pageRenderRequestHandler;
    }

    public void handleComponentEvent(ComponentEventRequestParameters parameters) throws IOException
    {
        componentEventRequestHandler.handle(parameters);
    }

    public void handlePageRender(PageRenderRequestParameters parameters) throws IOException
    {
        pageRenderRequestHandler.handle(parameters);
    }
}
