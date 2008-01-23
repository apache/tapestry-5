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

import org.apache.tapestry.services.Request;
import org.apache.tapestry.services.RequestFilter;
import org.apache.tapestry.services.RequestHandler;
import org.apache.tapestry.services.Response;

import java.io.IOException;
import java.util.Collection;
import java.util.regex.Pattern;

public class IgnoredPathsFilter implements RequestFilter
{
    private final Pattern[] _ignoredPatterns;

    public IgnoredPathsFilter(Collection<String> configuration)
    {
        _ignoredPatterns = new Pattern[configuration.size()];

        int i = 0;

        for (String regexp : configuration)
        {
            Pattern p = Pattern.compile(regexp, Pattern.CASE_INSENSITIVE);

            _ignoredPatterns[i++] = p;
        }
    }

    public boolean service(Request request, Response response, RequestHandler handler) throws IOException
    {
        String path = request.getPath();

        for (Pattern p : _ignoredPatterns)
        {
            if (p.matcher(path).matches()) return false;
        }

        return handler.service(request, response);
    }
}
