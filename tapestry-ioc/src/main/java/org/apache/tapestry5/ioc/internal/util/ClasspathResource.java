// Copyright 2006, 2007, 2008 The Apache Software Foundation
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

package org.apache.tapestry5.ioc.internal.util;

import org.apache.tapestry5.ioc.Resource;
import static org.apache.tapestry5.ioc.internal.util.Defense.notNull;

import java.net.URL;

/**
 * Implementation of {@link Resource} for files on the classpath (as defined by a {@link ClassLoader}).
 */
public final class ClasspathResource extends AbstractResource
{
    private final ClassLoader classLoader;

    // Guarded by this
    private URL url;

    // Guarded by this
    private boolean urlResolved;

    public ClasspathResource(String path)
    {
        this(Thread.currentThread().getContextClassLoader(), path);
    }

    public ClasspathResource(ClassLoader classLoader, String path)
    {
        super(path);

        notNull(classLoader, "classLoader");

        this.classLoader = classLoader;
    }

    @Override
    protected Resource newResource(String path)
    {
        return new ClasspathResource(classLoader, path);
    }

    public synchronized URL toURL()
    {
        if (!urlResolved)
        {
            url = classLoader.getResource(getPath());
            urlResolved = true;
        }

        return url;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == null) return false;

        if (obj == this) return true;

        if (obj.getClass() != getClass()) return false;

        ClasspathResource other = (ClasspathResource) obj;

        return other.classLoader == classLoader && other.getPath().equals(getPath());
    }

    @Override
    public int hashCode()
    {
        return 227 ^ getPath().hashCode();
    }

    @Override
    public String toString()
    {
        return "classpath:" + getPath();
    }

}
