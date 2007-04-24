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

import java.io.IOException;

import org.apache.tapestry.internal.TapestryInternalUtils;
import org.apache.tapestry.internal.structure.Page;
import org.apache.tapestry.services.ActionResponseGenerator;
import org.apache.tapestry.services.ComponentClassResolver;
import org.apache.tapestry.services.Dispatcher;
import org.apache.tapestry.services.Request;
import org.apache.tapestry.services.Response;

/**
 * Dispatches incoming requests for render requests. Render requests consist of either just a
 * logical page name (case insensitive) or a logical page name plus additional context. Because of
 * this structure, it take a little bit of work to identify the split point between the page name
 * and the context.
 */
public class PageRenderDispatcher implements Dispatcher
{
    private final ComponentClassResolver _componentClassResolver;

    private final PageLinkHandler _handler;

    private final PageResponseRenderer _renderer;

    public PageRenderDispatcher(ComponentClassResolver componentClassResolver,
            PageLinkHandler handler, PageResponseRenderer renderer)
    {
        _componentClassResolver = componentClassResolver;
        _handler = handler;
        _renderer = renderer;

    }

    public boolean dispatch(Request request, final Response response) throws IOException
    {
        // Rememeber that the path starts with a leading slash that is not part of the logical page
        // name.

        String path = request.getPath();

        // TAPESTRY-1343: This can happen in Tomcat (but not in Jetty) for URL such as
        // http://.../context (with no trailing slash).
        if (path.equals("")) return false;

        int searchStart = 1;

        while (true)
        {
            int nextslashx = path.indexOf('/', searchStart);

            boolean atEnd = nextslashx < 0;

            String pageName = atEnd ? path.substring(1) : path.substring(1, nextslashx);

            if (_componentClassResolver.isPageName(pageName))
            {
                String[] context = atEnd ? new String[0] : convertActivationContext(path
                        .substring(nextslashx + 1));

                PageRenderer renderer = new PageRenderer()
                {
                    public void renderPage(Page page)
                    {
                        try
                        {
                            _renderer.renderPageResponse(page, response);
                        }
                        catch (IOException ex)
                        {
                            new RuntimeException(ex);
                        }
                    }
                };

                ActionResponseGenerator responseGenerator = _handler.handle(
                        pageName,
                        context,
                        renderer);

                if (responseGenerator != null) responseGenerator.sendClientResponse(response);

                return true;
            }

            if (atEnd) return false;

            // Advance to the next slash within the path.

            searchStart = nextslashx + 1;
        }
    }

    /**
     * Converts the "extra path", the portion after the page name (and after the slash seperating
     * the page name from the activation context) into an array of strings. LinkFactory and friends
     * URL encode each value, so we URL decode the value (we assume that page names are "URL safe").
     * 
     * @param extraPath
     * @return
     */
    private String[] convertActivationContext(String extraPath)
    {
        if (extraPath.length() == 0) return new String[0];

        String[] context = extraPath.split("/");

        for (int i = 0; i < context.length; i++)
        {
            context[i] = TapestryInternalUtils.urlDecode(context[i]);
        }

        return context;
    }
}
