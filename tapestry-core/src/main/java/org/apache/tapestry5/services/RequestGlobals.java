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

package org.apache.tapestry5.services;

import org.apache.tapestry5.services.Request;
import org.apache.tapestry5.services.Response;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Service used to store the current request objects, both the Servlet API versions, and the Tapestry generic versions.
 * The service has a per-thread scope.
 */
public interface RequestGlobals
{
    /**
     * Stores the servlet API request and response objects, for access via the properties.
     */
    void storeServletRequestResponse(HttpServletRequest request, HttpServletResponse response);

    /**
     * The Servlet API Request. This is exposed as service HTTPServletRequest.
     */
    HttpServletRequest getHTTPServletRequest();

    HttpServletResponse getHTTPServletResponse();

    void storeRequestResponse(Request request, Response response);

    /**
     * The current request. This is exposed as service Request.
     */
    Request getRequest();

    /**
     * The current response. This is exposed as service Response.
     */
    Response getResponse();
}
