// Copyright 2021 The Apache Software Foundation
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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import javax.servlet.http.HttpServletResponse;

import org.apache.tapestry5.StreamResponse;
import org.apache.tapestry5.http.Link;

/**
 * <p>
 * An event handler method may return an instance of this class to send an specific HTTP status
 * code to the client. It also supports providing a string to be used as the response body
 * and extra HTTP headers to be set. This class also provides some utility
 * static methods for creating instances for specific HTTP statuses and a fluent API for setting
 * additional information on them.
 * </p>
 * 
 * <p>
 * For returning binary content and/or adding a response header more than once and/or
 * adding a response header without overwriting existing ones, implementing a {@link StreamResponse} 
 * is the most probable better choice.
 * </p>
 *
 * @since 5.8.0
 */
public final class HttpStatus
{
    private static final String CONTENT_LOCATION_HTTP_HEADER = "Content-Location";
    
    private static final String LOCATION_HTTP_HEADER = "Location";

    private final int statusCode;

    private String responseBody;
    
    private String contentType;
    
    private Map<String, String> extraHttpHeaders;
    
    /**
     * Creates an instance with status code <code>200 OK</code>.
     */
    public static HttpStatus ok()
    {
        return new HttpStatus(HttpServletResponse.SC_OK);
    }

    /**
     * Creates an instance with status code <code>201 Created</code>.
     */
    public static HttpStatus created()
    {
        return new HttpStatus(HttpServletResponse.SC_CREATED);
    }

    /**
     * Creates an instance with status code <code>202 Accepted</code>.
     */
    public static HttpStatus accepted()
    {
        return new HttpStatus(HttpServletResponse.SC_ACCEPTED);
    }
    
    /**
     * Creates an instance with status code <code>404 Not Found</code>.
     */
    public static HttpStatus notFound()
    {
        return new HttpStatus(HttpServletResponse.SC_NOT_FOUND);
    }
    
    /**
     * Creates an instance with status code <code>403 Forbidden</code>.
     */
    public static HttpStatus forbidden()
    {
        return new HttpStatus(HttpServletResponse.SC_FORBIDDEN);
    }
    
    /**
     * Creates an instance with status code <code>400 Bad Request</code>.
     */
    public static HttpStatus badRequest()
    {
        return new HttpStatus(HttpServletResponse.SC_BAD_REQUEST);
    }
    
    /**
     * Creates an instance with status code <code>401 Unauthorized</code>.
     */
    public static HttpStatus unauthorized()
    {
        return new HttpStatus(HttpServletResponse.SC_UNAUTHORIZED);
    }
    
    /**
     * Creates an instance with status code <code>303 See Other</code>.
     * @param location the value of the <code>Location</code> header.
     */
    public static HttpStatus seeOther(String location)
    {
        return new HttpStatus(HttpServletResponse.SC_SEE_OTHER).withLocation(location);
    }
    
    /**
     * Creates an instance with status code <code>303 See Also</code>.
     * @param location the value of the <code>Location</code> header.
     */
    public static HttpStatus seeOther(Link location)
    {
        return new HttpStatus(HttpServletResponse.SC_SEE_OTHER).withLocation(location);
    }
    
    /**
     * Creates an instance with status code <code>301 Moved Permanently</code>.
     * @param location the value of the <code>Location</code> header.
     */
    public static HttpStatus movedPermanently(String location)
    {
        return new HttpStatus(HttpServletResponse.SC_MOVED_PERMANENTLY).withLocation(location);
    }
    
    /**
     * Creates an instance with status code <code>301 Moved Permanently</code>.
     * @param link the value of the <code>Location</code> header.
     */
    public static HttpStatus movedPermanently(Link link)
    {
        return movedPermanently(link.toRedirectURI());
    }
    
    /**
     * Creates an instance with status code <code>302 Found</code>.
     * @param location the value of the <code>Location</code> header.
     */
    public static HttpStatus temporaryRedirect(String location)
    {
        return new HttpStatus(HttpServletResponse.SC_FOUND).withLocation(location);
    }
    
    /**
     * Creates an instance with status code <code>302 Found</code>.
     * @param location the value of the <code>Location</code> header.
     */
    public static HttpStatus temporaryRedirect(Link location)
    {
        return temporaryRedirect(location.toRedirectURI());
    }

    /**
     * Creates an object with a given status code and no response body.
     */
    public HttpStatus(int statusCode)
    {
        this(statusCode, null, null);
    }
    
    /**
     * Creates an object with a given status code, response body and <code>text/plain</code> MIME content type.
     */
    public HttpStatus(int statusCode, String responseBody)
    {
        this(statusCode, responseBody, "text/plain");
    }

    /**
     * Creates an object with a given status code, response body and MIME content type.
     */
    public HttpStatus(int statusCode, String responseBody, String contentType)
    {
        this.statusCode = statusCode;
        this.responseBody = responseBody;
        this.contentType = contentType;
    }
    
    /**
     * Sets a redirect by using the <code>Location</code> HTTP header.
     */
    public HttpStatus withLocation(Link location)
    {
        return withLocation(location.toRedirectURI());
    }
    
    /**
     * Sets a redirect by using the <code>Location</code> HTTP header.
     */
    public HttpStatus withLocation(String location)
    {
        return withHttpHeader(LOCATION_HTTP_HEADER, location);
    }

    /**
     * Sets the <code>Content-Location</code> HTTP header.
     */
    public HttpStatus withContentLocation(String location)
    {
        return withHttpHeader(CONTENT_LOCATION_HTTP_HEADER, location);
    }
    
    /**
     * Sets the <code>Content-Location</code> HTTP header.
     */
    public HttpStatus withContentLocation(Link link)
    {
        return withHttpHeader(CONTENT_LOCATION_HTTP_HEADER, link.toRedirectURI());
    }
    
    /**
     * Sets an HTTP header. If an existing value for this header already exists,
     * it gets overwritten. If you need to set multiple headers or add them without
     * overwriting existing ones, you need to implement {@link StreamResponse} instead.
     */
    public HttpStatus withHttpHeader(String name, String value)
    {
        Objects.requireNonNull(name, "Parameter name cannot be null");
        Objects.requireNonNull(value, "Parameter value cannot be null");
        if (extraHttpHeaders == null)
        {
            extraHttpHeaders = new HashMap<>(3);
        }
        extraHttpHeaders.put(name, value);
        return this;
    }

    /**
     * Returns the status code.
     */
    public int getStatusCode()
    {
        return statusCode;
    }

    /**
     * Returns the response body.
     */
    public String getResponseBody()
    {
        return responseBody;
    }

    /**
     * Returns the MIME content type of the response body.
     */
    public String getContentType() 
    {
        return contentType;
    }

    /**
     * Returns the extra HTTP headers.
     */
    @SuppressWarnings("unchecked")
    public Map<String, String> getExtraHttpHeaders() {
        return extraHttpHeaders != null ? extraHttpHeaders : Collections.EMPTY_MAP;
    }

}
