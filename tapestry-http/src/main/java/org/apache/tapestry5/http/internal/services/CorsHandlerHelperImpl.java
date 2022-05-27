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

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.tapestry5.http.TapestryHttpSymbolConstants;
import org.apache.tapestry5.http.services.CorsHandler;
import org.apache.tapestry5.http.services.CorsHandlerHelper;
import org.apache.tapestry5.ioc.annotations.Symbol;

/**
 * Default {@link CorsHandlerHelper} implementation.
 * 
 * @see CorsHandler
 * @see TapestryHttpSymbolConstants#CORS_ENABLED 
 * @see TapestryHttpSymbolConstants#CORS_ALLOWED_ORIGINS
 * @since 5.8.2
 */
public class CorsHandlerHelperImpl implements CorsHandlerHelper 
{
    
    private final List<String> allowedOrigins;
    
    private final boolean allowAllOrigins;
    private final boolean allowCredentials;
    private final String allowMethods;
    private final String allowedHeaders;
    private final String exposeHeaders;
    private final String maxAge;
    
    public CorsHandlerHelperImpl(
            @Symbol(TapestryHttpSymbolConstants.CORS_ALLOWED_ORIGINS) String allowedOrigins,
            @Symbol(TapestryHttpSymbolConstants.CORS_ALLOW_CREDENTIALS) boolean allowCredentials,
            @Symbol(TapestryHttpSymbolConstants.CORS_ALLOW_METHODS) String allowMethods,
            @Symbol(TapestryHttpSymbolConstants.CORS_ALLOWED_HEADERS) String allowedHeaders,
            @Symbol(TapestryHttpSymbolConstants.CORS_EXPOSE_HEADERS) String exposeHeaders,
            @Symbol(TapestryHttpSymbolConstants.CORS_MAX_AGE) String maxAge)
    {
        allowAllOrigins = ORIGIN_WILDCARD.equals(allowedOrigins);
        this.allowedOrigins = !allowAllOrigins ? Arrays.asList(trim(allowedOrigins.split(","))) : null;
        this.allowCredentials = allowCredentials;
        this.allowMethods = allowMethods.trim();
        this.allowedHeaders = allowedHeaders.trim();
        this.exposeHeaders = exposeHeaders.trim();
        this.maxAge = maxAge.trim();
    }

    @Override
    public String getPath(HttpServletRequest request) 
    {
        
        // Copied from RequestImpl.getRequest()
        String pathInfo = request.getPathInfo();

        if (pathInfo == null)
        {
            return request.getServletPath();
        }

        // Websphere 6.1 is a bit wonky (see TAPESTRY-1713), and tends to return the empty string
        // for the servlet path, and return the true path in pathInfo.

        return pathInfo.length() == 0 ? "/" : pathInfo;
    }

    @Override
    public Optional<String> getAllowedOrigin(HttpServletRequest request) 
    {
        final Optional<String> allowedOrigin;
        final Optional<String> origin = getOrigin(request);
    
        if (allowAllOrigins)
        {
            allowedOrigin = Optional.of(ORIGIN_WILDCARD);
        }
        else if (origin.isPresent() && allowedOrigins.contains(origin.get()))
        {
            allowedOrigin = origin;
        }
        else
        {
            allowedOrigin = Optional.empty();
        }
        
        return allowedOrigin;
    }

    @Override
    public Optional<String> getOrigin(HttpServletRequest request) 
    {
        return Optional.ofNullable(request.getHeader(ORIGIN_HEADER));
    }

    @Override
    public boolean isPreflight(HttpServletRequest request) 
    {
        boolean preflight = false;
        if (OPTIONS_METHOD.equals(request.getMethod()))
        {
            final Optional<String> origin = getAllowedOrigin(request);
            preflight = origin.isPresent() && !origin.get().trim().isEmpty();
        }
        return preflight;
    }
    
    @Override
    public void configureOrigin(HttpServletResponse response, String value) 
    {
        response.setHeader(ALLOW_ORIGIN_HEADER, value);
        addValueToVaryHeader(response, ORIGIN_HEADER);
    }

    @Override
    public void configureCredentials(HttpServletResponse response) 
    {
        if (allowCredentials)
        {
            response.setHeader(ALLOW_CREDENTIALS_HEADER, "true");
        }
    }

    @Override
    public void configureMethods(HttpServletResponse response) 
    {
        response.setHeader(ALLOW_METHODS_HEADER, allowMethods);
    }

    @Override
    public void configureAllowedHeaders(HttpServletResponse response, HttpServletRequest request) 
    {
        String value;
        if (allowedHeaders.isEmpty())
        {
            value = request.getHeader(REQUEST_HEADERS_HEADER);
            addValueToVaryHeader(response, REQUEST_HEADERS_HEADER);
        }
        else 
        {
            value = allowedHeaders;
        }
        if (value != null && !value.isEmpty())
        {
            response.setHeader(ALLOW_HEADERS_HEADER, value);
        }
    }
    

    @Override
    public void configureExposeHeaders(HttpServletResponse response) 
    {
        if (!exposeHeaders.isEmpty())
        {
            response.setHeader(EXPOSE_HEADERS_HEADER, exposeHeaders);
        }
    }
    
    @Override
    public void configureMaxAge(HttpServletResponse response) 
    {
        if (!maxAge.isEmpty())
        {
            response.setHeader(MAX_AGE_HEADER, maxAge);
        }
    }
    

    private static String[] trim(String[] strings) {
        for (int i = 0; i < strings.length; i++)
        {
            strings[i] = strings[i].trim();
        }
        return strings;
    }

}
