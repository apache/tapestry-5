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

import java.util.List;
import java.util.Locale;

/**
 * Generic version of {@link javax.servlet.http.HttpServletRequest}, used to encapsulate the Servlet API version, and to
 * help bridge the differences between Servlet API and Porlet API.
 * <p/>
 * <p/>
 * The Request service is a {@linkplain org.apache.tapestry5.ioc.services.PropertyShadowBuilder shadow} of the current
 * thread's request.
 */
public interface Request
{
    /**
     * Gets the {@link Session}. If create is false and the session has not be created previously, returns null. Also,
     * if the session is invalided and create is false, returns null.
     *
     * @param create true to force the creation of the session
     * @return the session (or null if create is false the session has not been previously created)
     */
    Session getSession(boolean create);

    /**
     * Returns the context path. This always starts with a "/" character and does not end with one, with the exception
     * of servlets in the root context, which return the empty string.
     */
    String getContextPath();

    /**
     * Returns a list of query parameter names, in alphabetical order.
     */
    List<String> getParameterNames();

    /**
     * Returns the query parameter value for the given name. Returns null if no such parameter is in the request. For a
     * multi-valued parameter, returns just the first value.
     */
    String getParameter(String name);

    /**
     * Returns the parameter values for the given name. Returns null if no such parameter is in the request.
     */
    String[] getParameters(String name);

    /**
     * Returns the path portion of the request, which starts with a "/" and contains everything up to the start of the
     * query parameters. It doesn't include the context path.
     */
    String getPath();

    /**
     * Returns the locale of the client as determined from the request headers.
     */
    Locale getLocale();

    /**
     * Returns the names of all headers in the request.
     */
    List<String> getHeaderNames();

    /**
     * Returns the value of the specified request header as a <code>long</code> value that represents a
     * <code>Date</code> object. Use this method with headers that contain dates, such as
     * <code>If-Modified-Since</code>.
     * <p/>
     * The date is returned as the number of milliseconds since January 1, 1970 GMT. The header name is case
     * insensitive.
     * <p/>
     * If the request did not have a header of the specified name, this method returns -1. If the header can't be
     * converted to a date, the method throws an <code>IllegalArgumentException</code>.
     *
     * @param name a <code>String</code> specifying the name of the header
     * @return a <code>long</code> value representing the date specified in the header expressed as the number of
     *         milliseconds since January 1, 1970 GMT, or -1 if the named header was not included with the reqest
     * @throws IllegalArgumentException If the header value can't be converted to a date
     */
    long getDateHeader(String name);

    /**
     * Returns the named header as a string, or null if not found.
     */
    String getHeader(String name);

    /**
     * Returns true if the request originated on the client using XmlHttpRequest (the core of any Ajax behavior). Ajax
     * action requests may behave quite differently than ordinary, page-based requests.  This implementation currently
     * depends on the client side setting a header: <strong>X-Requested-With=XMLHttpRequest</strong> (this is what
     * Prototype does).
     *
     * @return true if the request has an XmlHttpRequest origin
     */
    boolean isXHR();

    /**
     * Returns a boolean indicating whether this request was made using a secure channel, such as HTTPS.
     *
     * @return a boolean indicating if the request was made using a secure channel
     */
    public boolean isSecure();

    /**
     * Returns the host name of the server to which the request was sent. It is the value of the part before ":" in the
     * <code>Host</code> header, if any, or the resolved server name, or the server IP address.
     *
     * @return the name of the server
     */
    public String getServerName();

    /**
     * Checks whether the requested session ID is still valid.
     *
     * @return true if the request included a session id that is still active, false if the included session id has
     *         expired
     */
    boolean isRequestedSessionIdValid();


    /**
     * Returns the value of the named attribute as an <code>Object</code>, or <code>null</code> if no attribute of the
     * given name exists.  Because this method is a wrapper around {@link javax.servlet.ServletRequest#getAttribute(String)},
     * it is case <em>sensitive</em> (unlike most of Tapestry).
     *
     * @param name a <code>String</code> specifying the name of the attribute
     * @return an <code>Object</code> containing the value of the attribute, or <code>null</code> if the attribute does
     *         not exist
     */
    Object getAttribute(String name);

    /**
     * Stores an attribute in this request. Attributes are reset between requests (and remember that in Tapestry, there
     * is usually two requests per operation: the action request that redirects to a render request).
     *
     * @param name  a <code>String</code> specifying the name of the attribute
     * @param value the <code>Object</code> to be stored, or null to remove the attribute
     */
    void setAttribute(String name, Object value);

    /**
     * Returns the name of the HTTP method with which this request was made, for example, GET, POST, or PUT.
     *
     * @return a string specifying the name of the method with which this request was made
     */
    public String getMethod();
}
