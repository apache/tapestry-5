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

package org.apache.tapestry5.internal.services;

import org.apache.tapestry5.internal.InternalConstants;
import org.apache.tapestry5.internal.structure.Page;
import org.apache.tapestry5.ioc.internal.util.Defense;
import org.apache.tapestry5.services.Request;

import java.io.IOException;

/**
 * Alternative implementation, used when {@link org.apache.tapestry5.SymbolConstants#SUPPRESS_REDIRECT_FROM_ACTION_REQUESTS}
 * is set to true.
 */
public class ImmediateActionRenderResponseGenerator implements ActionRenderResponseGenerator
{
    private final Request request;

    public ImmediateActionRenderResponseGenerator(Request request)
    {
        this.request = request;
    }

    public void generateResponse(Page page) throws IOException
    {
        Defense.notNull(page, "page");

        // This can happen when the ComponentEventRequestHandlerImpl notices that the response
        // is not yet committed, and sets up to render a default response for the page containing
        // the component.
        if (request.getAttribute(InternalConstants.IMMEDIATE_RESPONSE_PAGE_ATTRIBUTE) != null) return;

        // We are somewhere in the middle of processing an action request, possibly something
        // complicated like a form submission.  Tapestry components are not re-entrant, so we
        // can't render the request right now, instead we record that we need to render
        // a response as an attribute, and let a filter on the ComponentEventRequestHandler service
        // do the work.

        request.setAttribute(InternalConstants.IMMEDIATE_RESPONSE_PAGE_ATTRIBUTE, page);
    }
}
