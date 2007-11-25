// Copyright 2006, 2007 The Apache Software Foundation
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

import org.apache.tapestry.ComponentEventHandler;
import org.apache.tapestry.TapestryConstants;
import org.apache.tapestry.internal.structure.Page;
import org.apache.tapestry.internal.util.Holder;
import org.apache.tapestry.runtime.Component;
import org.apache.tapestry.services.ComponentEventResultProcessor;
import org.apache.tapestry.services.PageRenderRequestHandler;
import org.apache.tapestry.services.Response;

import java.io.IOException;

/**
 * Handles a PageLink as specified by a PageLinkPathSource by activating and then rendering the
 * page.
 */
public class PageRenderRequestHandlerImpl implements PageRenderRequestHandler
{
    private final RequestPageCache _cache;

    private final ComponentEventResultProcessor _resultProcessor;

    private final PageResponseRenderer _pageResponseRenderer;

    private final Response _response;

    public PageRenderRequestHandlerImpl(RequestPageCache cache, ComponentEventResultProcessor resultProcessor,
                                        PageResponseRenderer pageResponseRenderer, Response response)
    {
        _cache = cache;
        _resultProcessor = resultProcessor;
        _pageResponseRenderer = pageResponseRenderer;
        _response = response;
    }

    public void handle(String logicalPageName, String[] context)
    {
        Page page = _cache.get(logicalPageName);

        final Holder<Boolean> holder = Holder.create();

        ComponentEventHandler handler = new ComponentEventHandler()
        {
            @SuppressWarnings("unchecked")
            public boolean handleResult(Object result, Component component, String methodDescription)
            {
                try
                {
                    _resultProcessor.processComponentEvent(result, component, methodDescription);
                }
                catch (IOException e)
                {
                    throw new RuntimeException(e);
                }

                holder.put(true);

                return true; // abort the event
            }
        };

        page.getRootElement().triggerEvent(TapestryConstants.ACTIVATE_EVENT, context, handler);

        // The handler will have asked the result processor to send a response.

        if (holder.hasValue()) return;

        try
        {
            _pageResponseRenderer.renderPageResponse(page, _response);
        }
        catch (IOException ex)
        {
            throw new RuntimeException(ex);
        }

    }
}
