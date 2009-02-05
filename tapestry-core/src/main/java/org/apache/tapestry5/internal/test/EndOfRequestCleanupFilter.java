// Copyright 2009 The Apache Software Foundation
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

package org.apache.tapestry5.internal.test;

import org.apache.tapestry5.services.RequestHandler;
import org.apache.tapestry5.services.Request;
import org.apache.tapestry5.services.Response;
import org.apache.tapestry5.services.RequestFilter;
import org.apache.tapestry5.ioc.services.PerthreadManager;

import java.io.IOException;

/**
 * Makes sure that {@link org.apache.tapestry5.ioc.services.PerthreadManager#cleanup()} is invoked at the end of each
 * request (normally handled by {@link org.apache.tapestry5.TapestryFilter}).
 */
public class EndOfRequestCleanupFilter implements RequestFilter
{
    private final PerthreadManager perThreadManager;

    public EndOfRequestCleanupFilter(PerthreadManager perThreadManager)
    {
        this.perThreadManager = perThreadManager;
    }

    public boolean service(Request request, Response response, RequestHandler handler) throws IOException
    {
        try
        {
            return handler.service(request, response);
        }
        finally
        {
            perThreadManager.cleanup();
        }
    }
}
