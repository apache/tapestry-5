// Copyright 2009 The Apache Software Foundation
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

package org.apache.tapestry5.ioc.services;

import org.apache.tapestry5.ioc.ObjectCreator;

/**
 * A <a href="http://en.wikipedia.org/wiki/Thunk">thunk</a> is a delayed calculation. In Java and Tapestry terms, a
 * Thunk is a proxy object of a particular interface that delegates all methods to an object of the same type obtained
 * from an {@link org.apache.tapestry5.ioc.ObjectProvider}. This is used by {@link
 * org.apache.tapestry5.ioc.services.LazyAdvisor} to build lazy thunk proxies.
 *
 * @since 5.1.0.1
 */
public interface ThunkCreator
{
    /**
     * Creates a Thunk of the given proxy type.
     *
     * @param proxyType     type of object to create (must be an interface)
     * @param objectCreator provides an instance of the same type on demand (may be invoked multiple times)
     * @param description   to be returned from the thunk's toString() method
     * @param <T>           type of thunk
     * @return thunk of given type
     */
    <T> T createThunk(Class<T> proxyType, ObjectCreator objectCreator, String description);
}
