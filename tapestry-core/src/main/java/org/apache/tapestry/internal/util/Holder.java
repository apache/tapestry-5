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

package org.apache.tapestry.internal.util;

/**
 * An object that holds some type of other object. This is useful for communicating information from
 * an inner class (used as a closure) to the containing method.
 * 
 * @param <T>
 */
public class Holder<T>
{
    private T _held;

    public void put(T object)
    {
        _held = object;
    }

    public T get()
    {
        return _held;
    }

    public boolean hasValue()
    {
        return _held != null;
    }

    public static <T> Holder<T> create()
    {
        return new Holder<T>();
    }
}
