// Copyright 2006 The Apache Software Foundation
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
import java.net.URL;

import org.apache.tapestry.services.Context;
import org.apache.tapestry.services.Request;
import org.apache.tapestry.services.RequestFilter;
import org.apache.tapestry.services.RequestHandler;
import org.apache.tapestry.services.Response;

/**
 * Identifies requests that are for actual resource files in the context. For those, Tapestry allows
 * the servlet container to process the request.
 */
public class StaticFilesFilter implements RequestFilter
{
    private final Context _context;

    public StaticFilesFilter(Context context)
    {
        _context = context;
    }

    public boolean service(Request request, Response response, RequestHandler handler)
            throws IOException
    {
        String path = request.getPath();

        URL url = _context.getResource(path);

        if (url != null)
            return false;

        return handler.service(request, response);
    }
}
