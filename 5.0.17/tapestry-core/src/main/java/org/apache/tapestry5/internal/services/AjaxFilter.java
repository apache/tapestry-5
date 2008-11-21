// Copyright 2007, 2008 The Apache Software Foundation
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

import org.apache.tapestry5.services.*;

import java.io.IOException;

/**
 * A filter that intercepts Ajax-oriented requests, thos that originate on the client-side using XmlHttpRequest. In
 * these cases, the action processing occurs normally, but the response is quite different.
 */
public class AjaxFilter implements ComponentEventRequestFilter
{
    private final Request request;

    private final ComponentEventRequestHandler ajaxHandler;

    public AjaxFilter(Request request, @Ajax ComponentEventRequestHandler ajaxHandler)
    {
        this.request = request;
        this.ajaxHandler = ajaxHandler;
    }

    public void handle(ComponentEventRequestParameters parameters, ComponentEventRequestHandler handler)
            throws IOException
    {
        ComponentEventRequestHandler next = request.isXHR() ? ajaxHandler : handler;

        next.handle(parameters);
    }

}
