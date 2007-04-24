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

package org.apache.tapestry.services;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;

import org.apache.tapestry.internal.services.URLEncoder;

/**
 * API agnostic wrapper for generating a response. Bridges the gaps between the Servlet API and the
 * Portlet API.
 */
public interface Response extends URLEncoder
{
    /**
     * Returns a PrintWriter object to which output may be sent. Invoking flush() on the writer will
     * commit the output.
     */
    PrintWriter getPrintWriter() throws IOException;

    /**
     * Returns an OutputStream to which byte-oriented output may be sent. Invoking flush() on the
     * stream will commit the output.
     */
    OutputStream getOutputStream() throws IOException;

    /**
     * Sends a redirect to the client.
     * 
     * @param URL
     *            full or partial (relative) URL to send to the client
     * @see #encodeRedirectURL(String)
     */
    void sendRedirect(String URL) throws IOException;

    /**
     * Sends an error response to the client using the specified status. The server defaults to
     * creating the response to look like an HTML-formatted server error page containing the
     * specified message, setting the content type to "text/html", leaving cookies and other headers
     * unmodified. If an error-page declaration has been made for the web application corresponding
     * to the status code passed in, it will be served back in preference to the suggested msg
     * parameter.
     * <p>
     * If the response has already been committed, this method throws an IllegalStateException.
     * After using this method, the response should be considered to be committed and should not be
     * written to.
     * 
     * @param sc
     *            the error status code
     * @param msg
     *            the descriptive message
     * @exception IOException
     *                If an input or output exception occurs
     * @exception IllegalStateException
     *                If the response was committed
     */
    void sendError(int sc, String message) throws IOException;

    /**
     * Sets the length of the content body in the response; this method sets the HTTP Content-Length
     * header.
     * 
     * @param length
     *            the length of the content
     */
    void setContentLength(int length);

    /**
     * Sets a response header with the given name and date-value. The date is specified in terms of
     * milliseconds since the epoch. If the header had already been set, the new value overwrites
     * the previous one.
     * 
     * @param name
     *            the name of the header to set
     * @param date
     *            the assigned date value
     */
    void setDateHeader(String name, long date);
}
