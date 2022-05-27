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
package org.apache.tapestry5.http.internal.services;

import java.io.IOException;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.tapestry5.http.CorsHandlerResult;
import org.apache.tapestry5.http.services.CorsHandler;
import org.apache.tapestry5.http.services.CorsHandlerHelper;

/**
 * <p>
 * Default {@link CorsHandler} implementation. It will process all requests with an Origin HTTP header,
 * regardless of path. It will also perform preflight requests if 
 * {@link CorsHandlerHelper#isPreflight(HttpServletRequest)}
 * returns <code>true</code>. Most logic is delegated is {@link CorsHandlerHelper}.
 * <p>
 * <p>
 * This implementation is inspired by the cors NPM module.
 * </p>
 * @see CorsHandlerHelper
 * @since 5.8.2
 */
public class DefaultCorsHandler implements CorsHandler 
{
    
    private final CorsHandlerHelper helper;

    public DefaultCorsHandler(CorsHandlerHelper helper) 
    {
        this.helper = helper;
    }

    @Override
    public CorsHandlerResult handle(HttpServletRequest request, HttpServletResponse response) throws IOException 
    {
        
        CorsHandlerResult result = CorsHandlerResult.CONTINUE_CORS_PROCESSING;
        final Optional<String> allowedOrigin = helper.getAllowedOrigin(request);
        if (helper.isPreflight(request))
        {
            if (allowedOrigin.isPresent())
            {
                helper.configureOrigin(response, allowedOrigin.get());
                helper.configureCredentials(response);
                helper.configureExposeHeaders(response);
                helper.configureMethods(response);
                helper.configureAllowedHeaders(response, request);
                helper.configureMaxAge(response);
                response.setContentLength(0);
                response.setStatus(HttpServletResponse.SC_NO_CONTENT);
                result = CorsHandlerResult.STOP_REQUEST_PROCESSING;
            }
        }
        else if (allowedOrigin.isPresent())
        {
            helper.configureOrigin(response, allowedOrigin.get());
            helper.configureCredentials(response);
            helper.configureExposeHeaders(response);
        }
        return result;
    }

}
