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
package org.apache.tapestry5.http.internal.services;

import java.util.Optional;

import javax.servlet.http.HttpServletRequest;

import org.apache.tapestry5.http.services.HttpRequestBodyConverter;
import org.apache.tapestry5.http.services.RestSupport;

/**
 * Default {@linkplain RestSupport} implementation.
 */
public class RestSupportImpl implements RestSupport 
{
    
    final private HttpServletRequest request;
    
    final private HttpRequestBodyConverter converter;

    public RestSupportImpl(final HttpServletRequest request, final HttpRequestBodyConverter converter) 
    {
        super();
        this.request = request;
        this.converter = converter;
    }

    @Override
    public boolean isHttpGet() 
    {
        return isMethod("GET");
    }

    @Override
    public boolean isHttpPost() 
    {
        return isMethod("POST");
    }

    @Override
    public boolean isHttpHead() 
    {
        return isMethod("HEAD");
    }

    @Override
    public boolean isHttpPut() 
    {
        return isMethod("PUT");
    }

    @Override
    public boolean isHttpDelete() 
    {
        return isMethod("DELETE");
    }

    @Override
    public boolean isHttpPatch() 
    {
        return isMethod("PATCH");
    }

    private boolean isMethod(String string) 
    {
        return request.getMethod().equals(string);
    }

    @Override
    public <T> Optional<T> getRequestBodyAs(Class<T> type) 
    {
        return Optional.ofNullable(converter.convert(request, type));
    }
    
}
