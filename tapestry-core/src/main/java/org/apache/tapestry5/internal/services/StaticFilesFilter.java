// Copyright 2006, 2007, 2008, 2010, 2011 The Apache Software Foundation
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
import java.net.URL;

import jakarta.servlet.http.HttpServletResponse;

import org.apache.tapestry5.TapestryConstants;
import org.apache.tapestry5.http.services.Context;
import org.apache.tapestry5.http.services.Request;
import org.apache.tapestry5.http.services.RequestFilter;
import org.apache.tapestry5.http.services.RequestHandler;
import org.apache.tapestry5.http.services.Response;

/**
 * Identifies requests that are for actual resource files in the context. For those, Tapestry allows the servlet
 * container to process the request.
 */
public class StaticFilesFilter implements RequestFilter
{
    private final Context context;

    public StaticFilesFilter(Context context)
    {
        this.context = context;
    }

    public boolean service(Request request, Response response, RequestHandler handler)
            throws IOException
    {
        String path = request.getPath();

        // TAPESTRY-1322: Treat requests from the browser for a favorites icon via the normal
        // servlet even if the file doesn't exist, to keep the request from looking like a
        // component action request.

        if (path.equals("/favicon.ico")) return false;

        // TAPESTRY-2606: A colon in the path is frequently the case for Tapestry event URLs,
        // but gives Windows fits.

        if (!path.contains(":"))
        {
            // We are making the questionable assumption that all files to be vended out will contain
            // an extension (with a dot separator). Without this, the filter tends to match against
            // folder names when we don't want it to (especially for the root context path).

            int dotx = path.lastIndexOf(".");

            if (dotx > 0)
            {
                URL url = context.getResource(path);

                if (url != null)
                {
                    String suffix = path.substring(dotx + 1);

                    // We never allow access to Tapestry component templates, even if they exist.
                    // It is considered a security risk, like seeing a raw JSP. Earlier alpha versions
                    // of Tapestry required that the templates be stored in WEB-INF.

                    if (suffix.equalsIgnoreCase(TapestryConstants.TEMPLATE_EXTENSION))
                    {

                        response.sendError(HttpServletResponse.SC_FORBIDDEN, String.format("URI %s may not be accessed remotely.", path));

                        return true;
                    }

                    return false;
                }
            }
        }

        return handler.service(request, response);
    }
}
