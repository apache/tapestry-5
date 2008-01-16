// Copyright 2008 The Apache Software Foundation
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

import org.apache.tapestry.TapestryConstants;
import org.apache.tapestry.internal.InternalConstants;
import org.apache.tapestry.internal.structure.Page;
import org.apache.tapestry.services.*;

import java.io.IOException;

public class ImmediateActionRenderResponseFilter implements ComponentActionRequestFilter
{
    private final Request _request;

    private final Response _response;

    private final PageResponseRenderer _renderer;

    public ImmediateActionRenderResponseFilter(Request request, PageResponseRenderer renderer, Response response)
    {
        _request = request;
        _renderer = renderer;
        _response = response;
    }

    public void handle(ComponentActionRequestParameters parameters, ComponentActionRequestHandler handler)
            throws IOException
    {
        handler.handle(parameters);

        // If markup or a redirect has already been generated, then we're good.

        if (_response.isCommitted()) return;

        // Otherwise, we should be operating in immediate mode.  Figure out which page
        // was selected to render.

        Page page = (Page) _request.getAttribute(InternalConstants.IMMEDIATE_RESPONSE_PAGE_ATTRIBUTE);

        if (page != null)
        {
            // We don't have a context to provide but this still nags me as not the right thing to do.

            page.getRootElement().triggerEvent(TapestryConstants.ACTIVATE_EVENT, new Object[0], null);

            _renderer.renderPageResponse(page);
            return;
        }

        throw new IllegalStateException(
                "Sanity check - neither a stream response nor a redirect response was generated for this action request.");
    }
}
