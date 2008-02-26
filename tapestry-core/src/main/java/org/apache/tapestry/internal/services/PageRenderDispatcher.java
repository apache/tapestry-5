// Copyright 2006, 2007, 2008 The Apache Software Foundation
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
import org.apache.tapestry.internal.TapestryInternalUtils;
import org.apache.tapestry.internal.URLEventContext;
import org.apache.tapestry.services.*;

import java.io.IOException;

/**
 * Dispatches incoming requests for render requests. Render requests consist of either just a logical page name (case
 * insensitive) or a logical page name plus additional context. Because of this structure, it take a little bit of work
 * to identify the split point between the page name and the context.
 */
public class PageRenderDispatcher implements Dispatcher
{
    private final ComponentClassResolver _componentClassResolver;

    private final PageRenderRequestHandler _handler;

    private final ContextValueEncoder _contextValueEncoder;

    public PageRenderDispatcher(ComponentClassResolver componentClassResolver, PageRenderRequestHandler handler,
                                ContextValueEncoder contextValueEncoder)
    {
        _componentClassResolver = componentClassResolver;
        _handler = handler;
        _contextValueEncoder = contextValueEncoder;
    }

    public boolean dispatch(Request request, final Response response) throws IOException
    {
        // Rememeber that the path starts with a leading slash that is not part of the logical page
        // name.

        String path = request.getPath();

        // TAPESTRY-1343: This can happen in Tomcat (but not in Jetty) for URL such as
        // http://.../context (with no trailing slash).
        if (path.equals("")) return false;

        int nextslashx = path.length();
        String pageName;
        boolean atEnd = true;

        while (true)
        {
            // TAPESTRY-2150: Look for the longest match, for situations where
            // you have some overlap between a class name and a package name.

            pageName = path.substring(1, nextslashx);

            if (!pageName.endsWith("/") && _componentClassResolver.isPageName(pageName)) break;

            nextslashx = path.lastIndexOf('/', nextslashx - 1);

            atEnd = false;

            if (nextslashx <= 1) return false;
        }


        String[] context = atEnd ? new String[0] : convertActivationContext(path
                .substring(nextslashx + 1));

        EventContext activationContext
                = new URLEventContext(_contextValueEncoder, context);

        PageRenderRequestParameters parameters = new PageRenderRequestParameters(pageName, activationContext);

        _handler.handle(parameters);

        return true;
    }

    /**
     * Converts the "extra path", the portion after the page name (and after the slash seperating the page name from the
     * activation context) into an array of strings. LinkFactory and friends URL encode each value, so we URL decode the
     * value (we assume that page names are "URL safe").
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
            context[i] = TapestryInternalUtils.unescapePercentAndSlash(context[i]);
        }

        return context;
    }
}
