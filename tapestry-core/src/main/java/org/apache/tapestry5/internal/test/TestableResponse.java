// Copyright 2007, 2009 The Apache Software Foundation
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

import java.util.List;

import org.apache.tapestry5.dom.Document;
import org.apache.tapestry5.http.Link;
import org.apache.tapestry5.http.services.Response;

public interface TestableResponse extends Response
{
    /**
     * Invoked as part of the rendering pipeline to store the final rendered Document object.
     */
    void setRenderedDocument(Document document);

    /**
     * Allows access to the rendered document.
     */
    Document getRenderedDocument();

    /**
     * Returns the link redirected to via {@link org.apache.tapestry5.http.services.Response#sendRedirect(org.apache.tapestry5.http.Link)}.
     */
    Link getRedirectLink();

    /**
     * Clears internal state, in preparation for the next test.
     */
    void clear();
    
    /**
     * Returns the named header.
     * 
     * @since 5.2.3
     */
    Object getHeader(String name);

    /**
     * Returns the values of a named header.
     * 
     * @since 5.4
     */
    List<?> getHeaders(String name);
    
    /**
     * Returns the redirect URL.
     * 
     * @since 5.2.3
     */
    String getRedirectURL();
    
    /**
     * Returns the status code for this response.
     * 
     * @since 5.2.3
     */
    int getStatus();
    
    /**
     * Returns the error message, if available.
     * 
     * @since 5.2.3
     */
    String getErrorMessage();
    
    /**
     * Returns the the MIME content type for the output.
     * 
     * @since 5.2.3
     */
    String getContentType();
    
    /**
     * Returns the content of the {@link javax.servlet.ServletOutputStream} as string.
     * 
     * @since 5.2.3
     */
    String getOutput();

}
