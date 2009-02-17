// Copyright 2008, 2009 The Apache Software Foundation
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

import java.io.IOException;

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
     * @param pageName page for the current request
     * @return true if a redirect was sent, false if normal processing should continue
     * @throws IOException
     */
    boolean checkForInsecureRequest(String pageName) throws IOException;

    /**
     * Determines if the page security does not match the request's security. If so, returns a base URL (to which the
     * context path and servlet path may be appended).
     *
     * @param pageName for the security check
     * @return a base URL when switching security levels, or null if the page's security matches the request security
     * @see org.apache.tapestry5.services.BaseURLSource#getBaseURL(boolean)
     */
    String getBaseURL(String pageName);
}
