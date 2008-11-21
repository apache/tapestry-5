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

import org.apache.tapestry5.ioc.ScopeConstants;
import org.apache.tapestry5.ioc.annotations.Scope;
import org.apache.tapestry5.services.Request;
import org.apache.tapestry5.services.RequestGlobals;
import org.apache.tapestry5.services.Response;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Dumb data holder for per-request data.
 */
@Scope(ScopeConstants.PERTHREAD)
public class RequestGlobalsImpl implements RequestGlobals
{
    private HttpServletRequest servletRequest;

    private HttpServletResponse servletResponse;

    private Request request;

    private Response response;

    public void storeServletRequestResponse(HttpServletRequest request, HttpServletResponse response)
    {
        servletRequest = request;
        servletResponse = response;
    }

    public HttpServletRequest getHTTPServletRequest()
    {
        return servletRequest;
    }

    public HttpServletResponse getHTTPServletResponse()
    {
        return servletResponse;
    }

    public void storeRequestResponse(Request request, Response response)
    {
        this.request = request;
        this.response = response;
    }

    public Request getRequest()
    {
        return request;
    }

    public Response getResponse()
    {
        return response;
    }
}
