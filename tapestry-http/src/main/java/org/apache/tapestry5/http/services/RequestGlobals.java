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

package org.apache.tapestry5.http.services;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Service used to store the current request objects, both the Servlet API versions, and the
 * Tapestry generic versions.
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

    /**
     * Stores the <a href="http://tapestry.apache.org/current/apidocs/org/apache/tapestry5/services/ComponentClassResolver.html#canonicalizePageName-java.lang.String-">canonicalized</a>
     * name of the active page for this request.
     * 
     * @param pageName
     *            name of page (probably extracted from the URL)
     * @since 5.2.0
     */
    void storeActivePageName(String pageName);

    /**
     * Returns the active page name previously stored.
     * 
     * @return canonicalized page name
     * @since 5.2.0
     */
    String getActivePageName();
}
