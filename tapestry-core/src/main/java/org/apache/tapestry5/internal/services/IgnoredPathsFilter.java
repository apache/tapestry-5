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

import org.apache.tapestry5.services.HttpServletRequestFilter;
import org.apache.tapestry5.services.HttpServletRequestHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collection;
import java.util.regex.Pattern;

public class IgnoredPathsFilter implements HttpServletRequestFilter
{
    private final Pattern[] ignoredPatterns;

    public IgnoredPathsFilter(Collection<String> configuration)
    {
        ignoredPatterns = new Pattern[configuration.size()];

        int i = 0;

        for (String regexp : configuration)
        {
            Pattern p = Pattern.compile(regexp, Pattern.CASE_INSENSITIVE);

            ignoredPatterns[i++] = p;
        }
    }

    public boolean service(HttpServletRequest request, HttpServletResponse response, HttpServletRequestHandler handler)
            throws IOException
    {
        // The servlet path should be "/", and path info is everything after that.

        String path = request.getServletPath();
        String pathInfo = request.getPathInfo();

        if (pathInfo != null) path += pathInfo;


        for (Pattern p : ignoredPatterns)
        {
            if (p.matcher(path).matches()) return false;
        }

        // Not a match, so let it go.

        return handler.service(request, response);
    }
}
