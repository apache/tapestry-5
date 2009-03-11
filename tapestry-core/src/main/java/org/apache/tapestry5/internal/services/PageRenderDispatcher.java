// Copyright 2006, 2007, 2008, 2009 The Apache Software Foundation
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

import org.apache.tapestry5.services.*;

import java.io.IOException;

/**
 * Dispatches incoming requests for render requests. Render requests consist of either just a logical page name (case
 * insensitive) or a logical page name plus additional context. Because of this structure, it take a little bit of work
 * to identify the split point between the page name and the context.
 */
public class PageRenderDispatcher implements Dispatcher
{
    private final ComponentRequestHandler componentRequestHandler;

    private final ComponentEventLinkEncoder linkEncoder;

    public PageRenderDispatcher(ComponentRequestHandler componentRequestHandler, ComponentEventLinkEncoder linkEncoder)
    {
        this.componentRequestHandler = componentRequestHandler;
        this.linkEncoder = linkEncoder;
    }

    public boolean dispatch(Request request, final Response response) throws IOException
    {

        PageRenderRequestParameters parameters = linkEncoder.decodePageRenderRequest(request);

        if (parameters == null) return false;

        componentRequestHandler.handlePageRender(parameters);

        return true;
    }
}
