// Copyright 2010 The Apache Software Foundation
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.tapestry5.ioc.services.Coercion;

/**
 * Functional operations on collections with generics support. Tending to use the equivalent names from
 * Clojure.
 * 
 * @since 5.2.0
 */
public class Func
{

    /**
     * Functional map (i.e., transform operation) from a List&lt;S&g;t to List&lt;T&gt;.
     */
    public static <S, T> List<T> map(List<S> source, Coercion<S, T> coercion)
    {
        Defense.notNull(source, "source");
        Defense.notNull(coercion, "coercion");

        int count = source.size();

        List<T> result = new ArrayList<T>(count);

        for (S s : source)
        {
            T t = coercion.coerce(s);

            result.add(t);
        }

        return result;
    }

    /**
     * Performs an operation on each element of the source collection.
     */
    public static <T> void each(Collection<T> source, Operation<T> operation)
    {
        for (T t : source)
        {
            operation.op(t);
        }
    }
}
