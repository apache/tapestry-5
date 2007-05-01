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

package org.apache.tapestry.ioc.internal.util;

import static org.apache.tapestry.ioc.internal.util.Defense.notBlank;
import static org.apache.tapestry.ioc.internal.util.Defense.notNull;

import java.net.URL;
import java.util.Locale;

import org.apache.tapestry.ioc.Resource;

/**
 * Abstract implementation of {@link Resource}. Subclasses must implement the abstract methods
 * {@link Resource#toURL()} and {@link #newResource(String)} as well as toString(), hashCode() and
 * equals().
 */
public abstract class AbstractResource implements Resource
{
    private final String _path;

    protected AbstractResource(String path)
    {
        notNull(path, "path");
        _path = path;

    }

    public final String getPath()
    {
        return _path;
    }

    public final String getFile()
    {
        int slashx = _path.lastIndexOf('/');

        return _path.substring(slashx + 1);
    }

    public final String getFolder()
    {
        int slashx = _path.lastIndexOf('/');

        return (slashx < 0) ? "" : _path.substring(0, slashx);
    }

    public final Resource forFile(String relativePath)
    {
        Defense.notNull(relativePath, "relativePath");

        StringBuilder builder = new StringBuilder(getFolder());

        for (String term : relativePath.split("/"))
        {
            // This will occur if the relative path contains sequential slashes

            if (term.equals("")) continue;

            if (term.equals(".")) continue;

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

            if (builder.length() > 0) builder.append("/");

            builder.append(term);
        }

        return createResource(builder.toString());
    }

    public final Resource forLocale(Locale locale)
    {
        for (String path : new LocalizedNameGenerator(_path, locale))
        {
            Resource potential = createResource(path);

            URL url = potential.toURL();

            if (url != null) return potential;
        }

        return null;
    }

    public final Resource withExtension(String extension)
    {
        notBlank(extension, "extension");

        int dotx = _path.lastIndexOf('.');

        if (dotx < 0) return createResource(_path + "." + extension);

        return createResource(_path.substring(0, dotx + 1) + extension);
    }

    /**
     * Creates a new resource, unless the path matches the current Resource's path (in which case,
     * this resource is returned).
     */
    private Resource createResource(String path)
    {
        if (_path.equals(path)) return this;

        return newResource(path);
    }

    /** Factory method provided by subclasses. */
    protected abstract Resource newResource(String path);
}
