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

import org.apache.tapestry.services.Request;
import org.apache.tapestry.services.RequestFilter;
import org.apache.tapestry.services.RequestHandler;
import org.apache.tapestry.services.Response;

import java.io.IOException;

/**
 * Responsible for determining the locale for the current request. Currently, this is based on the
 * client's web browser. Later extensions will store the current locale as a cookie, or as a session
 * attribute.
 */
public class LocalizationFilter implements RequestFilter
{
    private final LocalizationSetter _setter;

    public LocalizationFilter(LocalizationSetter setter)
    {
        _setter = setter;
    }

    public boolean service(Request request, Response response, RequestHandler handler)
            throws IOException
    {
        _setter.setThreadLocale(request.getLocale());

        return handler.service(request, response);
    }

}
