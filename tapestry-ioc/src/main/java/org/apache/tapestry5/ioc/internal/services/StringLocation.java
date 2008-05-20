// Copyright 2007 The Apache Software Foundation
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

package org.apache.tapestry5.ioc.internal.services;

import org.apache.tapestry5.ioc.Location;
import org.apache.tapestry5.ioc.Resource;

/**
 * Implementation of {@link Location} used when the underlying resource isn't really known.
 */
public final class StringLocation implements Location
{
    private final String description;

    private final int line;

    public StringLocation(String description, int line)
    {
        this.description = description;
        this.line = line;
    }

    @Override
    public String toString()
    {
        return description;
    }

    /**
     * Returns 0.
     */
    public int getColumn()
    {
        return 0;
    }

    public int getLine()
    {
        return line;
    }

    /**
     * Returns null; we don't know where the file really is (it's probably a class on the class path).
     */
    public Resource getResource()
    {
        return null;
    }

}
