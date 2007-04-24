// Copyright 2006, 2007 The Apache Software Foundation
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

import static org.apache.tapestry.ioc.internal.util.CollectionFactory.newList;
import static org.apache.tapestry.ioc.internal.util.CollectionFactory.newStack;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.servlet.ServletContext;

import org.apache.tapestry.ioc.util.Stack;
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

    @SuppressWarnings("unchecked")
    public List<String> getResourcePaths(String path)
    {
        List<String> result = newList();
        Stack<String> queue = newStack();

        queue.push(path);

        while (!queue.isEmpty())
        {
            String current = queue.pop();

            Set<String> matches = (Set<String>) _servletContext.getResourcePaths(current);

            // Tomcat 5.5.20 inside JBoss 4.0.2 has been observed to do this!
            // Perhaps other servers do as well.

            if (matches == null)
                continue;

            for (String match : matches)
            {
                // Folders are queued up for further expansion.

                if (match.endsWith("/"))
                    queue.push(match);
                else
                    result.add(match);
            }

        }

        Collections.sort(result);

        return result;
    }
}
