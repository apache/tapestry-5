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

package org.apache.tapestry.ioc.internal.util;

import org.apache.tapestry.ioc.Resource;
import static org.apache.tapestry.ioc.internal.util.Defense.notNull;

import java.net.URL;

/**
 * Implementation of {@link Resource} for files on the classpath (as defined by a
 * {@link ClassLoader}).
 */
public final class ClasspathResource extends AbstractResource
{
    private final ClassLoader _classLoader;

    private URL _url;

    public ClasspathResource(String path)
    {
        this(Thread.currentThread().getContextClassLoader(), path);
    }

    public ClasspathResource(ClassLoader classLoader, String path)
    {
        super(path);

        notNull(classLoader, "classLoader");

        _classLoader = classLoader;
    }

    @Override
    protected Resource newResource(String path)
    {
        return new ClasspathResource(_classLoader, path);
    }

    public synchronized URL toURL()
    {
        if (_url == null) _url = _classLoader.getResource(getPath());

        return _url;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == null) return false;

        if (obj == this) return true;

        if (obj.getClass() != getClass()) return false;

        ClasspathResource other = (ClasspathResource) obj;

        return other._classLoader == _classLoader && other.getPath().equals(getPath());
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
