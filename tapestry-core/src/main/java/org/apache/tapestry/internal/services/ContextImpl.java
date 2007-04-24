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

package org.apache.tapestry.internal.services;

import java.net.MalformedURLException;
import java.net.URL;

import javax.servlet.ServletContext;

import org.apache.tapestry.services.Context;



public class ContextImpl implements Context
{
    private final ServletContext _servletContext;

    public ContextImpl(ServletContext servletContext)
    {
        _servletContext = servletContext;
    }

    public URL getResource(String path)
    {
        try
        {
            return _servletContext.getResource(path);
        }
        catch (MalformedURLException ex)
        {
            throw new RuntimeException(ex);
        }
    }

    public String getInitParameter(String name)
    {
        return _servletContext.getInitParameter(name);
    }

}
