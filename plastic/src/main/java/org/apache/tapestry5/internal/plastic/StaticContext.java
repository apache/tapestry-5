// Copyright 2011 The Apache Software Foundation
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

package org.apache.tapestry5.internal.plastic;

import java.util.ArrayList;
import java.util.List;

/**
 * Stores static context information needed by a transformed PlasticClass; this includes data such as
 * injections.
 */
public class StaticContext
{
    private final List<Object> values;

    public StaticContext()
    {
        this(new ArrayList<Object>());
    }

    private StaticContext(List<Object> values)
    {
        this.values = values;
    }

    /**
     * Duplicates this StaticContext, which is used when creating a subclass
     * of a transformed class.
     * 
     * @return new StaticContext with the same values
     */
    public StaticContext dupe()
    {
        return new StaticContext(new ArrayList<Object>(values));
    }

    /** Store a context value and return its index. */
    public int store(Object value)
    {
        values.add(value);

        return values.size() - 1;
    }

    public Object get(int index)
    {
        return values.get(index);
    }

}
