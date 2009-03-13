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

package org.apache.tapestry5.ioc;

import org.apache.tapestry5.ioc.internal.util.Defense;

/**
 * A wrapper that allows objects of a target type to be ordered. Each Orderable object is given a unique id and a set of
 * pre-requisites (objects which should be ordered earlier) and post-requisites (objects which should be ordered
 * later).
 *
 * @param <T>
 */
public class Orderable<T>
{
    private final String id;

    private final T target;

    private final String[] constraints;

    /**
     * @param id     unique identifier for the target object
     * @param target the object to be ordered; this may also be null (in which case the id represents a placeholder)
     */

    public Orderable(String id, T target, String... constraints)
    {
        this.id = Defense.notBlank(id, "id");
        this.target = target;
        this.constraints = constraints;
    }

    public String getId()
    {
        return id;
    }

    public T getTarget()
    {
        return target;
    }

    public String[] getConstraints()
    {
        return constraints;
    }

    @Override
    public String toString()
    {
        StringBuilder buffer = new StringBuilder("Orderable[");

        buffer.append(id);

        for (String c : constraints)
        {
            buffer.append(" ");
            buffer.append(c);
        }

        buffer.append(" ");
        buffer.append(target.toString());
        buffer.append("]");

        return buffer.toString();
    }
}
