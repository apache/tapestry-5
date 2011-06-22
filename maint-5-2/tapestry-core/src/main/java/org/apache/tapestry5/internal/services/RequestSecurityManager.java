// Copyright 2008, 2009, 2010 The Apache Software Foundation
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.apache.tapestry5.internal.services;

import java.io.IOException;

import org.apache.tapestry5.services.ComponentEventRequestParameters;
import org.apache.tapestry5.services.PageRenderRequestParameters;

/**
 * Used to manage the relationship between the security of a request and the security of a page. By secure, we mean
 * whether a request uses HTTPS and whether a page demands the use of HTTPS.
 * 
 * @see org.apache.tapestry5.services.Request#isSecure()
 */
public interface RequestSecurityManager
{
    /**
     * Checks the page to see if it is secure; if so, and the request is not secure, then a redirect to the page is
     * generated and sent.
     * 
     * @param parameters
     *            parameters for the current request
     * @return true if a redirect was sent, false if normal processing should continue
     * @throws IOException
     */
    boolean checkForInsecurePageRenderRequest(PageRenderRequestParameters parameters) throws IOException;

    /**
     * Checks the target page of the component event request to see if it is secure; if so, and the
     * request is not secure, then a redirect to the page is generated and sent, preserving the
     * original component event request.
     * 
     * @param parameters
     *            parameters for the current request
     * @return true if a redirect was sent, false if normal processing should continue
     * @throws IOException
     * @since 5.2.0.0
     */
    boolean checkForInsecureComponentEventRequest(ComponentEventRequestParameters parameters) throws IOException;

    /**
     * Determines if the page security does not match the request's security.
     * 
     * @param pageName
     *            for the security check
     * @return SECURE or INSECURE if a change in security is required, or UNSPECIFIED if the request security matches
     *         the page's security level
     */
    LinkSecurity checkPageSecurity(String pageName);
}
