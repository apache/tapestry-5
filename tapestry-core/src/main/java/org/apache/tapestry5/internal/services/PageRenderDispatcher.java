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

package org.apache.tapestry5.internal.services;

import org.apache.tapestry5.EventContext;
import org.apache.tapestry5.internal.TapestryInternalUtils;
import org.apache.tapestry5.internal.URLEventContext;
import org.apache.tapestry5.services.*;

import java.io.IOException;

/**
 * Dispatches incoming requests for render requests. Render requests consist of either just a logical page name (case
 * insensitive) or a logical page name plus additional context. Because of this structure, it take a little bit of work
 * to identify the split point between the page name and the context.
 */
public class PageRenderDispatcher implements Dispatcher
{
    private final ComponentClassResolver componentClassResolver;

    private final PageRenderRequestHandler handler;

    private final ContextValueEncoder contextValueEncoder;

    public PageRenderDispatcher(ComponentClassResolver componentClassResolver, PageRenderRequestHandler handler,
                                ContextValueEncoder contextValueEncoder)
    {
        this.componentClassResolver = componentClassResolver;
        this.handler = handler;
        this.contextValueEncoder = contextValueEncoder;
    }

    public boolean dispatch(Request request, final Response response) throws IOException
    {

        // The extended name may include a page activation context. The trick is
        // to figure out where the logical page name stops and where the
        // activation context begins. Further, strip out the leading slash.

        String path = request.getPath();

        // TAPESTRY-1343: Sometimes path is the empty string (it should always be at least a slash,
        // but Tomcat may return the empty string for a root context request).

        String extendedName = path.length() == 0 ? path : path.substring(1);

        // Ignore trailing slashes in the path.
        while (extendedName.endsWith("/"))
            extendedName = extendedName.substring(0, extendedName.length() - 1);

        int slashx = extendedName.length();
        boolean atEnd = true;

        while (slashx > 0)
        {

            String pageName = extendedName.substring(0, slashx);
            String pageActivationContext = atEnd ? "" :
                                           extendedName.substring(slashx + 1);

            if (process(pageName, pageActivationContext)) return true;

            // Work backwards, splitting at the next slash.
            slashx = extendedName.lastIndexOf('/', slashx - 1);

            atEnd = false;
        }

        // OK, maybe its all page activation context for the root Index page.

        return process("", extendedName);
    }

    private boolean process(String pageName, String pageActivationContext) throws IOException
    {
        if (!componentClassResolver.isPageName(pageName)) return false;

        String[] values = convertActivationContext(pageActivationContext);

        EventContext activationContext
                = new URLEventContext(contextValueEncoder, values);

        PageRenderRequestParameters parameters = new PageRenderRequestParameters(pageName, activationContext);

        handler.handle(parameters);

        return true;
    }

    /**
     * Converts the "extra path", the portion after the page name (and after the slash seperating the page name from the
     * activation context) into an array of strings. LinkFactory and friends URL encode each value, so we URL decode the
     * value (we assume that page names are "URL safe").
     */
    private String[] convertActivationContext(String extraPath)
    {
        if (extraPath.length() == 0) return new String[0];

        String[] context = TapestryInternalUtils.splitPath(extraPath);

        for (int i = 0; i < context.length; i++)
        {
            context[i] = TapestryInternalUtils.unescapePercentAndSlash(context[i]);
        }

        return context;
    }
}
