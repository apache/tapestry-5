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

package org.apache.tapestry5.ioc.internal.util;

import org.apache.tapestry5.ioc.Location;
import org.apache.tapestry5.ioc.Resource;

import java.util.Formatter;

/**
 * Implementation class for {@link org.apache.tapestry5.ioc.Location}.
 */
public final class LocationImpl implements Location
{
    private final Resource resource;

    private final int line;

    private final int column;

    private static final int UNKNOWN = -1;

    public LocationImpl(Resource resource)
    {
        this(resource, UNKNOWN);
    }

    public LocationImpl(Resource resource, int line)
    {
        this(resource, line, UNKNOWN);
    }

    public LocationImpl(Resource resource, int line, int column)
    {
        this.resource = resource;
        this.line = line;
        this.column = column;
    }

    public Resource getResource()
    {
        return resource;
    }

    public int getLine()
    {
        return line;
    }

    public int getColumn()
    {
        return column;
    }

    @Override
    public String toString()
    {
        StringBuilder buffer = new StringBuilder(resource.toString());
        Formatter formatter = new Formatter(buffer);

        if (line != UNKNOWN) formatter.format(", line %d", line);

        if (column != UNKNOWN) formatter.format(", column %d", column);

        return buffer.toString();
    }

    @Override
    public int hashCode()
    {
        final int PRIME = 31;
        int result = 1;
        result = PRIME * result + column;
        result = PRIME * result + line;
        result = PRIME * result + ((resource == null) ? 0 : resource.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        final LocationImpl other = (LocationImpl) obj;
        if (column != other.column) return false;
        if (line != other.line) return false;
        if (resource == null)
        {
            if (other.resource != null) return false;
        }
        else if (!resource.equals(other.resource)) return false;
        return true;
    }

}
