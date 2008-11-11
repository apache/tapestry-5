// Copyright 2008 The Apache Software Foundation
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

/**
 * Used with {@link org.apache.tapestry5.internal.services.InternalClassTransformation} to search for prior injections
 * of a give type and value.  Assumes the values have a reasonable hashCode() implementation.
 */
public final class InjectionKey
{
    private final Class type;
    private final Object value;

    private final int hashCode;

    public InjectionKey(Class type, Object value)
    {
        this.type = type;
        this.value = value;

        hashCode = type.hashCode() * 31 + value.hashCode();
    }

    @Override
    public int hashCode()
    {
        return hashCode;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == null) return false;

        if (obj instanceof InjectionKey)
        {
            InjectionKey other = (InjectionKey) obj;

            return type.equals(other.type) &&
                    value.equals(other.value);
        }

        return false;
    }

    @Override
    public String toString()
    {
        return String.format("InjectionKey[%s %s]", type.getName(), value);
    }
}
