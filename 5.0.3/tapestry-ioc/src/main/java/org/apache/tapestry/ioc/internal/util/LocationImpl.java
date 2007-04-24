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

import java.util.Formatter;

import org.apache.tapestry.ioc.Location;
import org.apache.tapestry.ioc.Resource;

/**
 * Implementation class for {@link org.apache.tapestry.ioc.Location}.
 */
public final class LocationImpl implements Location
{
    private final Resource _resource;

    private final int _line;

    private final int _column;

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
        _resource = resource;
        _line = line;
        _column = column;
    }

    public Resource getResource()
    {
        return _resource;
    }

    public int getLine()
    {
        return _line;
    }

    public int getColumn()
    {
        return _column;
    }

    @Override
    public String toString()
    {
        StringBuilder buffer = new StringBuilder(_resource.toString());
        Formatter formatter = new Formatter(buffer);

        if (_line != UNKNOWN) formatter.format(", line %d", _line);

        if (_column != UNKNOWN) formatter.format(", column %d", _column);

        return buffer.toString();
    }

    @Override
    public int hashCode()
    {
        final int PRIME = 31;
        int result = 1;
        result = PRIME * result + _column;
        result = PRIME * result + _line;
        result = PRIME * result + ((_resource == null) ? 0 : _resource.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        final LocationImpl other = (LocationImpl) obj;
        if (_column != other._column) return false;
        if (_line != other._line) return false;
        if (_resource == null)
        {
            if (other._resource != null) return false;
        }
        else if (!_resource.equals(other._resource)) return false;
        return true;
    }

}
