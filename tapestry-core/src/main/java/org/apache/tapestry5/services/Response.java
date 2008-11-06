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

import org.apache.tapestry5.Link;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;

/**
 * API agnostic wrapper for generating a response. Bridges the gaps between the Servlet API and the Portlet API.
 * <p/>
 * <p/>
 * The Response service is a {@linkplain org.apache.tapestry5.ioc.services.PropertyShadowBuilder shadow} of the current
 * thread's response object.
 */
public interface Response
{
    /**
     * Returns a PrintWriter object to which output may be sent. Invoking flush() on the writer will commit the output.
     *
     * @param contentType the MIME content type for the output, typically "text/html"
     */
    PrintWriter getPrintWriter(String contentType) throws IOException;

    /**
     * Returns an OutputStream to which byte-oriented output may be sent. Invoking flush() on the stream will commit the
     * output.
     *
     * @param contentType the MIME content type for the output, often "application/octet-stream" or "text/plain" or one
     *                    of several others
     */
    OutputStream getOutputStream(String contentType) throws IOException;

    /**
     * Sends a redirect to the client.
     *
     * @param URL full or partial (relative) URL to send to the client
     * @see #encodeRedirectURL(String)
     */
    void sendRedirect(String URL) throws IOException;

    /**
     * Sends a redirect to a link.
     *
     * @param link link to redirect to.
     */
    void sendRedirect(Link link) throws IOException;

    /**
     * Sets the status code for this response.  This method is used to set the return status code when there is no error
     * (for example, for the status codes SC_OK or SC_MOVED_TEMPORARILY).  If there is an error, and the caller wishes
     * to invoke an error-page defined in the web applicaion, the <code>sendError</code> method should be used instead.
     *
     * @param sc the status code
     */
    public void setStatus(int sc);

    /**
     * Sends an error response to the client using the specified status. The server defaults to creating the response to
     * look like an HTML-formatted server error page containing the specified message, setting the content type to
     * "text/html", leaving cookies and other headers unmodified. If an error-page declaration has been made for the web
     * application corresponding to the status code passed in, it will be served back in preference to the suggested msg
     * parameter.
     * <p/>
     * If the response has already been committed, this method throws an IllegalStateException. After using this method,
     * the response should be considered to be committed and should not be written to.
     *
     * @param sc      the error status code
     * @param message the descriptive message
     * @throws IOException           If an input or output exception occurs
     * @throws IllegalStateException If the response was committed
     */
    void sendError(int sc, String message) throws IOException;

    /**
     * Sets the length of the content body in the response; this method sets the HTTP Content-Length header.
     *
     * @param length the length of the content
     */
    void setContentLength(int length);

    /**
     * Sets a response header with the given name and date-value. The date is specified in terms of milliseconds since
     * the epoch. If the header had already been set, the new value overwrites the previous one.
     *
     * @param name the name of the header to set
     * @param date the assigned date value
     */
    void setDateHeader(String name, long date);

    /**
     * Sets a response header with the given name and value. If the header had already been set, the new value
     * overwrites the previous one.
     *
     * @param name  the name of the header to set
     * @param value the assigned value
     */
    void setHeader(String name, String value);

    /**
     * Sets a response header with the given name and integer value. If the header had already been set, the new value
     * overwrites the previous one.
     *
     * @param name  the name of the header to set
     * @param value the assigned integer value
     */
    void setIntHeader(String name, int value);

    /**
     * Encodes the URL, ensuring that a session id is included (if a session exists, and as necessary depending on the
     * client browser's use of cookies).
     *
     * @param URL
     * @return the same URL or a different one with additional information to track the user session
     */
    String encodeURL(String URL);

    /**
     * Encodes the URL for use as a redirect, ensuring that a session id is included (if a session exists, and as
     * necessary depending on the client browser's use of cookies).
     *
     * @param URL
     * @return the same URL or a different one with additional information to track the user session
     */
    String encodeRedirectURL(String URL);

    /**
     * Returns true if the response has already been sent, either as a redirect or as a stream of content.
     *
     * @return true if response already sent
     */
    boolean isCommitted();
}
