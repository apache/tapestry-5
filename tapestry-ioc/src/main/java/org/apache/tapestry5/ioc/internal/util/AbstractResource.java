// Copyright 2006, 2008, 2010, 2011, 2012 The Apache Software Foundation
//
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

package org.apache.tapestry5.ioc.internal.util;

import org.apache.tapestry5.ioc.Resource;
import org.apache.tapestry5.ioc.util.LocalizedNameGenerator;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Locale;

/**
 * Abstract implementation of {@link Resource}. Subclasses must implement the abstract methods {@link Resource#toURL()}
 * and {@link #newResource(String)} as well as toString(), hashCode() and equals().
 */
public abstract class AbstractResource extends LockSupport implements Resource
{
    private class Localization
    {
        final Locale locale;

        final Resource resource;

        final Localization next;

        private Localization(Locale locale, Resource resource, Localization next)
        {
            this.locale = locale;
            this.resource = resource;
            this.next = next;
        }
    }

    private final String path;

    // Guarded by Lock
    private boolean exists, existsComputed;

    // Guarded by lock
    private Localization firstLocalization;

    protected AbstractResource(String path)
    {
        assert path != null;
        this.path = path;
    }

    public final String getPath()
    {
        return path;
    }

    public final String getFile()
    {
        int slashx = path.lastIndexOf('/');

        return path.substring(slashx + 1);
    }

    public final String getFolder()
    {
        int slashx = path.lastIndexOf('/');

        return (slashx < 0) ? "" : path.substring(0, slashx);
    }

    public final Resource forFile(String relativePath)
    {
        assert relativePath != null;
        StringBuilder builder = new StringBuilder(getFolder());

        for (String term : relativePath.split("/"))
        {
            // This will occur if the relative path contains sequential slashes

            if (term.equals(""))
                continue;

            if (term.equals("."))
                continue;

            if (term.equals(".."))
            {
                int slashx = builder.lastIndexOf("/");

                // TODO: slashx < 0 (i.e., no slash)

                // Trim path to content before the slash

                builder.setLength(slashx);
                continue;
            }

            // TODO: term blank or otherwise invalid?
            // TODO: final term should not be "." or "..", or for that matter, the
            // name of a folder, since a Resource should be a file within
            // a folder.

            if (builder.length() > 0)
                builder.append("/");

            builder.append(term);
        }

        return createResource(builder.toString());
    }

    public final Resource forLocale(Locale locale)
    {
        try
        {
            acquireReadLock();

            for (Localization l = firstLocalization; l != null; l = l.next)
            {
                if (l.locale.equals(locale))
                {
                    return l.resource;
                }
            }

            return populateLocalizationCache(locale);
        } finally
        {
            releaseReadLock();
        }
    }

    private Resource populateLocalizationCache(Locale locale)
    {
        try
        {
            upgradeReadLockToWriteLock();

            // Race condition: another thread may have beaten us to it:

            for (Localization l = firstLocalization; l != null; l = l.next)
            {
                if (l.locale.equals(locale))
                {
                    return l.resource;
                }
            }

            Resource result = findLocalizedResource(locale);

            firstLocalization = new Localization(locale, result, firstLocalization);

            return result;

        } finally
        {
            downgradeWriteLockToReadLock();
        }
    }

    private Resource findLocalizedResource(Locale locale)
    {
        for (String path : new LocalizedNameGenerator(this.path, locale))
        {
            Resource potential = createResource(path);

            if (potential.exists())
                return potential;
        }

        return null;
    }

    public final Resource withExtension(String extension)
    {
        assert InternalUtils.isNonBlank(extension);
        int dotx = path.lastIndexOf('.');

        if (dotx < 0)
            return createResource(path + "." + extension);

        return createResource(path.substring(0, dotx + 1) + extension);
    }

    /**
     * Creates a new resource, unless the path matches the current Resource's path (in which case, this resource is
     * returned).
     */
    private Resource createResource(String path)
    {
        if (this.path.equals(path))
            return this;

        return newResource(path);
    }

    /**
     * Simple check for whether {@link #toURL()} returns null or not.
     */
    public boolean exists()
    {
        try
        {
            acquireReadLock();

            if (!existsComputed)
            {
                computeExists();
            }

            return exists;
        } finally
        {
            releaseReadLock();
        }
    }

    private void computeExists()
    {
        try
        {
            upgradeReadLockToWriteLock();

            if (!existsComputed)
            {
                exists = toURL() != null;
                existsComputed = true;
            }
        } finally
        {
            downgradeWriteLockToReadLock();
        }
    }

    /**
     * Obtains the URL for the Resource and opens the stream, wrapped by a BufferedInputStream.
     */
    public InputStream openStream() throws IOException
    {
        URL url = toURL();

        if (url == null)
            return null;

        return new BufferedInputStream(url.openStream());
    }

    /**
     * Factory method provided by subclasses.
     */
    protected abstract Resource newResource(String path);
}
