// Copyright 2007, 2008 The Apache Software Foundation
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

package org.apache.tapestry.internal.services;

import org.apache.tapestry.EventContext;
import org.apache.tapestry.internal.EmptyEventContext;
import org.apache.tapestry.services.*;

import java.io.IOException;

/**
 * Recognizes a request for the application root (i.e., "/") and handles this the same as a render
 * request for the "Start" page.
 */
public class RootPathDispatcher implements Dispatcher
{
    private final ComponentClassResolver _componentClassResolver;

    private final PageRenderRequestHandler _handler;

    private final String _startPageName;

    private static final EventContext EMPTY_CONTEXT = new EmptyEventContext();

    private final PageRenderRequestParameters _parameters;

    public RootPathDispatcher(ComponentClassResolver componentClassResolver,
                              PageRenderRequestHandler handler, String startPageName)
    {
        _componentClassResolver = componentClassResolver;
        _handler = handler;
        _startPageName = startPageName;

        _parameters = new PageRenderRequestParameters(_startPageName, EMPTY_CONTEXT);
    }

    public boolean dispatch(Request request, final Response response) throws IOException
    {
        // Only match the root path

        if (request.getPath().equals("/") && _componentClassResolver.isPageName(_startPageName))
        {
            _handler.handle(_parameters);

            return true;
        }

        return false;
    }

}
