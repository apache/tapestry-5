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

package org.apache.tapestry5.internal.util;

import java.util.Arrays;

/**
 * Combines multiple values to form a single composite key. MultiKey can often be used as an alternative to nested
 * maps.
 */
public final class MultiKey
{
    private static final int PRIME = 31;

    private final Object[] values;

    private final int hashCode;

    /**
     * Creates a new instance from the provided values. It is assumed that the values provided are good map keys
     * themselves -- immutable, with proper implementations of equals() and hashCode().
     *
     * @param values
     */
    public MultiKey(Object... values)
    {
        this.values = values;

        hashCode = PRIME * Arrays.hashCode(this.values);
    }

    @Override
    public int hashCode()
    {
        return hashCode;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final MultiKey other = (MultiKey) obj;

        return Arrays.equals(values, other.values);
    }

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder("MultiKey[");

        boolean first = true;

        for (Object o : values)
        {
            if (!first)
                builder.append(", ");

            builder.append(o);

            first = false;
        }

        builder.append("]");

        return builder.toString();
    }

}
