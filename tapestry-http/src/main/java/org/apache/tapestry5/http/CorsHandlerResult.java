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

package org.apache.tapestry5.http;

import org.apache.tapestry5.http.services.CorsHandler;
import org.apache.tapestry5.http.services.CorsHttpServletRequestFilter;
import org.apache.tapestry5.http.services.HttpServletRequestFilter;

/**
 * Enumeration that defines the possible outcomes of 
 * {@link CorsHandler#handle(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)}
 * @see CorsHandler
 * @see CorsHttpServletRequestFilter
 * @since 5.8.2
 */
public enum CorsHandlerResult
{
    /**
     * The next {@linkplain CorsHandler} should be called. 
     * Usually returned when the CORS handler didn't actually processed the request.
     */
    CONTINUE_CORS_PROCESSING,
    
    /**
     * No other {@linkplain CorsHandler} should be called and request processing
     * should resume (i.e. {@linkplain CorsHttpServletRequestFilter} calls 
     * {@link HttpServletRequestFilter#service(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, org.apache.tapestry5.http.services.HttpServletRequestHandler)}.
     * Usually returned when the handler actually processed the request.
     */
    CONTINUE_REQUEST_PROCESSING,
    
    /**
     * The request processing should stop immediately. Usually used with CORS preflight requests.
     */
    STOP_REQUEST_PROCESSING
}
