// Copyright 2006, 2008 The Apache Software Foundation
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

import org.apache.tapestry5.ioc.Resource;
import org.apache.tapestry5.ioc.internal.util.AbstractResource;
import static org.apache.tapestry5.ioc.internal.util.Defense.notNull;
import org.apache.tapestry5.services.Context;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * A resource stored with in the web application context.
 */
public class ContextResource extends AbstractResource
{
    private static final int PRIME = 37;

    private final Context context;

    public ContextResource(Context context, String path)
    {
        super(path);

        notNull(context, "context");

        this.context = context;
    }

    @Override
    public String toString()
    {
        return String.format("context:%s", getPath());
    }

    @Override
    protected Resource newResource(String path)
    {
        return new ContextResource(context, path);
    }

    public URL toURL()
    {
        // This is so easy to screw up; ClassLoader.getResource() doesn't want a leading slash,
        // and HttpServletContext.getResource() does. This is what I mean when I say that
        // a framework is an accumulation of the combined experience of many users and developers.

        String contextPath = "/" + getPath();

        // Always prefer the actual file to the URL.  This is critical for templates to
        // reload inside Tomcat.

        File file = context.getRealFile(contextPath);

        if (file != null && file.exists())
        {
            try
            {
                return file.toURL();
            }
            catch (MalformedURLException ex)
            {
                throw new RuntimeException(ex);
            }
        }

        // But, when packaged inside a WAR or JAR, the File will not be available, so use whatever
        // URL we get ... but reloading won't work.

        return context.getResource(contextPath);
    }

    @Override
    public int hashCode()
    {
        return PRIME * context.hashCode() + getPath().hashCode();
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;

        final ContextResource other = (ContextResource) obj;

        return context == other.context && getPath().equals(other.getPath());
    }

}
