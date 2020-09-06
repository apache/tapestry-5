// Copyright 2006-2013 The Apache Software Foundation
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

import java.net.URL;

import org.apache.tapestry5.commons.Resource;

/**
 * Implementation of {@link Resource} for files on the classpath (as defined by a {@link ClassLoader}).
 */
public final class ClasspathResource extends AbstractResource
{
    private final ClassLoader classLoader;

    // Guarded by lock
    private URL url;

    // Guarded by lock
    private boolean urlResolved;

    public ClasspathResource(String path)
    {
        this(Thread.currentThread().getContextClassLoader(), path);
    }

    public ClasspathResource(ClassLoader classLoader, String path)
    {
        super(path);
        assert classLoader != null;

        this.classLoader = classLoader;
    }

    @Override
    protected Resource newResource(String path)
    {
        return new ClasspathResource(classLoader, path);
    }

    @Override
    public URL toURL()
    {
        try
        {
            acquireReadLock();

            if (!urlResolved)
            {
                resolveURL();
            }

            return url;
        } finally
        {
            releaseReadLock();
        }
    }

    private void resolveURL()
    {
        try
        {
            upgradeReadLockToWriteLock();

            if (!urlResolved)
            {
                url = classLoader.getResource(getPath());

                validateURL(url);

                urlResolved = true;
            }
        } finally
        {
            downgradeWriteLockToReadLock();
        }


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
