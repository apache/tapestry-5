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

package org.apache.tapestry5.ioc;

/**
 * Used when filtering a collection of objects of a given type; the predicate is passed
 * each object in turn, and returns true to include the object in the result collection.
 * 
 * @since 5.2.0
 */
public interface Predicate<T>
{
    /**
     * Examines the object and determines whether to accept or reject it.
     * 
     * @return true to accept, false to reject
     */
    boolean accept(T object);
}
