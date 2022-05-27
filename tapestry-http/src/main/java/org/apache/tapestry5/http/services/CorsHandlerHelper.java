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
package org.apache.tapestry5.http.services;

import java.util.Optional;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.tapestry5.http.TapestryHttpSymbolConstants;

/**
 * Service that provides useful methods for {@link CorsHandler} implementations.
 * 
 * @see CorsHandler
 * @since 5.8.2
 */
public interface CorsHandlerHelper {
    
    /**
     * Returns the requested URL path, using the same logic as {@link Request#getPath()}.
     * @param request an {@link HttpServletRequest}.
     * @return a {@link String} with the path.
     */
    String getPath(HttpServletRequest request);
    
    /**
     * Returns the value of the Origin HTTP header.
     * @param request an {@link HttpServletRequest}.
     * @return an {@link Optional} wrapping the Origin HTTP header value.
     */
    Optional<String> getOrigin(HttpServletRequest request);

    /**
     * Tells whether this request is a CORS preflight one (i.e. HTTP method OPTION and 
     * non-empty Origin HTTP header).
     * 
     * @param request an {@link HttpServletRequest}.
     * @return <code>true</code> if it's a preflight request, <code>false</code> otherwise.
     */
    boolean isPreflight(HttpServletRequest request);
    
    /**
     * Returns the origin of this requests, if it's allowed. 
     * @param request an {@link HttpServletRequest}.
     * @return an {@link Optional} wrapping the allowed origin.
     * @see TapestryHttpSymbolConstants#CORS_ALLOWED_ORIGINS
     */
    Optional<String> getAllowedOrigin(HttpServletRequest request);
    
    /**
     * Sets the Access-Control-Allow-Origin HTTP header with a given value.
     * @param value a {@linkplain String}.
     * @param response an {@link HttpServletResponse} instance.
     * @see #ALLOW_ORIGIN_HEADER
     */
    void configureOrigin(HttpServletResponse response, String value);

    /**
     * Conditionally sets the Access-Control-Allow-Credentials HTTP header.
     * Out-of-the-box, this is done based on the 
     * {@link TapestryHttpSymbolConstants#CORS_ALLOW_CREDENTIALS} symbol.
     * @param response an {@link HttpServletResponse}.
     * @see TapestryHttpSymbolConstants#CORS_ALLOW_CREDENTIALS
     */
    void configureCredentials(HttpServletResponse response);

    /**
     * Conditionally sets the Access-Control-Allow-Methods HTTP header in responses
     * to preflight CORS requests.
     * Out-of-the-box, the value comes from 
     * {@link TapestryHttpSymbolConstants#CORS_ALLOW_CREDENTIALS} symbol
     * and the header is only set if the value isn't empty.
     * @param response an {@link HttpServletResponse}.
     * @see TapestryHttpSymbolConstants#CORS_ALLOW_CREDENTIALS
     */
    void configureMethods(HttpServletResponse response);
    
    /**
     * Conditionally the Access-Control-Request-Headers HTTP header.
     * Out-of-the-box, the value comes from 
     * {@link TapestryHttpSymbolConstants#CORS_ALLOWED_HEADERS} symbol if not empty.
     * Otherwise, it comes from the value of the same HTTP header from the request, 
     * also if not empty. Otherwise, the header isn't set.
     * @param response an {@link HttpServletResponse}.
     * @param request an {@link HttpServletRequest}.
     * @see TapestryHttpSymbolConstants#CORS_ALLOWED_HEADERS
     */
    void configureAllowedHeaders(HttpServletResponse response, HttpServletRequest request);
    
    /**
     * Conditionally sets the Access-Control-Expose-Headers HTTP header.
     * Out-of-the-box, the value comes from 
     * {@link TapestryHttpSymbolConstants#CORS_EXPOSE_HEADERS} symbol, if not empty.
     * Otherwise, the header isn't set.
     * @param response an {@link HttpServletResponse}.
     * @see TapestryHttpSymbolConstants#CORS_EXPOSE_HEADERS
     */
    void configureExposeHeaders(HttpServletResponse response);

    /**
     * Conditionally sets the Access-Control-Max-Age HTTP header.
     * Out-of-the-box, the value comes from 
     * {@link TapestryHttpSymbolConstants#CORS_MAX_AGE} symbol, if not empty.
     * Otherwise, the header isn't set.
     * @param response an {@link HttpServletResponse}.
     * @see TapestryHttpSymbolConstants#CORS_MAX_AGE
     */
    void configureMaxAge(HttpServletResponse response);
    
    /**
     * Adds a value to the Vary HTTP header.
     * @param response an {@link HttpServletResponse} instance.
     * @param value the value to be added. 
     */
    default void addValueToVaryHeader(HttpServletResponse response, String value)
    {
        final String vary = response.getHeader(VARY_HEADER);
        response.setHeader(VARY_HEADER, vary == null ? value : vary + ", " + value);
    }
    
    /**
     * Name of the Origin HTTP header.
     */
    String ORIGIN_HEADER = "Origin";

    /**
     * Name of the Access-Control-Allow-Origin HTTP header.
     */
    String ALLOW_ORIGIN_HEADER = "Access-Control-Allow-Origin";

    /**
     * Name of the Access-Control-Allow-Credentials HTTP header.
     */
    String ALLOW_CREDENTIALS_HEADER = "Access-Control-Allow-Credentials";
    
    /**
     * Name of the Access-Control-Allow-Methods HTTP header.
     */
    String ALLOW_METHODS_HEADER = "Access-Control-Allow-Methods";

    /**
     * Name of the Access-Control-Request-Headers HTTP header.
     */
    String REQUEST_HEADERS_HEADER = "Access-Control-Request-Headers";

    /**
     * Name of the Access-Control-Allow-Headers HTTP header.
     */
    String ALLOW_HEADERS_HEADER = "Access-Control-Allow-Headers";
    
    /**
     * Name of the Access-Control-Expose-Headers HTTP header.
     */
    String EXPOSE_HEADERS_HEADER = "Access-Control-Expose-Headers";

    /**
     * Name of the Access-Control-Max-Age HTTP header.
     */
    String MAX_AGE_HEADER = "Access-Control-Max-Age";

    /**
     * Name of the Vary HTTP header.
     */
    String VARY_HEADER = "Vary";

    /**
     * OPTIONS HTTP method name.
     */
    String OPTIONS_METHOD = "OPTIONS";

    /**
     * The CORS Origin wildcard.
     */
    String ORIGIN_WILDCARD = "*";

}
