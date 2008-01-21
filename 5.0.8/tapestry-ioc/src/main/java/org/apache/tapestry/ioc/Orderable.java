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

package org.apache.tapestry.ioc;

import static org.apache.tapestry.ioc.internal.util.Defense.notBlank;

/**
 * A wrapper that allows objects of a target type to be ordered. Each Orderable object is given a
 * unique id and a set of pre-requisites (objects which should be ordered earlier) and
 * post-requisites (objects which should be ordered later).
 *
 * @param <T>
 */
public class Orderable<T>
{
    private final String _id;

    private final T _target;

    private final String[] _constraints;

    /**
     * @param id     unique identifier for the target object
     * @param target the object to be ordered; this may also be null (in which case the id represents a
     *               placeholder)
     */

    public Orderable(String id, T target, String... constraints)
    {
        _id = notBlank(id, "id");
        _target = target;
        _constraints = constraints;
    }

    public String getId()
    {
        return _id;
    }

    public T getTarget()
    {
        return _target;
    }

    public String[] getConstraints()
    {
        return _constraints;
    }

    @Override
    public String toString()
    {
        StringBuilder buffer = new StringBuilder("Orderable[");

        buffer.append(_id);

        for (String c : _constraints)
        {
            buffer.append(" ");
            buffer.append(c);
        }

        buffer.append(" ");
        buffer.append(_target.toString());
        buffer.append("]");

        return buffer.toString();
    }
}
