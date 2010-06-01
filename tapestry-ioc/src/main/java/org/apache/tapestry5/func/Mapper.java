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

package org.apache.tapestry5.func;

/**
 * Base class used with {@link Flow#map(Mapper)} to
 * define how Flow values are mapped from one type
 * to another (or otherwise transformed).
 * 
 * @since 5.2.0
 */
public abstract class Mapper<S, T>
{
    /** Implemented in subclasses to map a source value to a target value. */
    public abstract T map(S value);

    /**
     * Combines this mapper (S --&gt;T) with another mapper (T --&gt;X) to form
     * a composite mapper (S --&gt; X).
     */
    public final <X> Mapper<S, X> combine(final Mapper<T, X> other)
    {
        assert other != null;
        
        final Mapper<S, T> stMapper = this;

        return new Mapper<S, X>()
        {
            public X map(S value)
            {

                T tValue = stMapper.map(value);

                return other.map(tValue);
            }
        };
    }
}
