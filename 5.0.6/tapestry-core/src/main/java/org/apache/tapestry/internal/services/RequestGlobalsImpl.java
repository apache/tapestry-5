// Copyright 2006, 2007 The Apache Software Foundation
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

import static org.apache.tapestry.ioc.IOCConstants.PERTHREAD_SCOPE;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.tapestry.ioc.annotations.Scope;
import org.apache.tapestry.services.Request;
import org.apache.tapestry.services.RequestGlobals;
import org.apache.tapestry.services.Response;

/**
 * Dumb data holder for per-request data.
 */
@Scope(PERTHREAD_SCOPE)
public class RequestGlobalsImpl implements RequestGlobals
{
    private HttpServletRequest _servletRequest;

    private HttpServletResponse _servletResponse;

    private Request _request;

    private Response _response;

    public void store(HttpServletRequest request, HttpServletResponse response)
    {
        _servletRequest = request;
        _servletResponse = response;
    }

    public HttpServletRequest getHTTPServletRequest()
    {
        return _servletRequest;
    }

    public HttpServletResponse getHTTPServletResponse()
    {
        return _servletResponse;
    }

    public void store(Request request, Response response)
    {
        _request = request;
        _response = response;
    }

    public Request getRequest()
    {
        return _request;
    }

    public Response getResponse()
    {
        return _response;
    }

}
