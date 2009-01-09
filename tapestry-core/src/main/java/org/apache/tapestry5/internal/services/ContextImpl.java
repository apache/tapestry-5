// Copyright 2006, 2007, 2008, 2009 The Apache Software Foundation
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

package org.apache.tapestry5.internal.services;

import org.apache.tapestry5.ioc.internal.util.CollectionFactory;
import org.apache.tapestry5.ioc.internal.util.InternalUtils;
import org.apache.tapestry5.ioc.util.Stack;
import org.apache.tapestry5.services.Context;

import javax.servlet.ServletContext;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class ContextImpl implements Context
{
    private final ServletContext servletContext;

    public ContextImpl(ServletContext servletContext)
    {
        this.servletContext = servletContext;
    }

    public URL getResource(String path)
    {
        try
        {
            return servletContext.getResource(path);
        }
        catch (MalformedURLException ex)
        {
            throw new RuntimeException(ex);
        }
    }

    public File getRealFile(String path)
    {
        String realPath = servletContext.getRealPath(path);

        return realPath == null ? null : new File(realPath);
    }

    public String getInitParameter(String name)
    {
        return servletContext.getInitParameter(name);
    }

    @SuppressWarnings("unchecked")
    public List<String> getResourcePaths(String path)
    {
        List<String> result = CollectionFactory.newList();
        Stack<String> queue = CollectionFactory.newStack();

        queue.push(path);

        while (!queue.isEmpty())
        {
            String current = queue.pop();

            Set<String> matches = servletContext.getResourcePaths(current);

            // Tomcat 5.5.20 inside JBoss 4.0.2 has been observed to do this!
            // Perhaps other servers do as well.

            if (matches == null) continue;

            for (String match : matches)
            {
                // Folders are queued up for further expansion.

                if (match.endsWith("/")) queue.push(match);
                else result.add(match);
            }
        }

        Collections.sort(result);

        return result;
    }

    public Object getAttribute(String name)
    {
        return servletContext.getAttribute(name);
    }

    public List<String> getAttributeNames()
    {
        return InternalUtils.toList(servletContext.getAttributeNames());
    }

    public String getMimeType(String file)
    {
        return servletContext.getMimeType(file);
    }
}
