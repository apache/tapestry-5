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

import java.util.HashMap;
import java.util.Map;

/**
 * Quick and dirty key/value cache that is subclassed to provide the logic that generates the value for
 * a missing key.
 * 
 * @param <S> key type
 * @param <T> value type
 */
public abstract class Cache<S, T>
{
    private Map<S, T> innerCache = new HashMap<S, T>();

    public T get(S input)
    {
        T result = innerCache.get(input);

        if (result == null)
        {
            result = convert(input);
            innerCache.put(input, result);
        }

        return result;
    }

    protected abstract T convert(S input);
}
