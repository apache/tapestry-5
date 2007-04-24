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

package org.apache.tapestry.services;

import org.apache.tapestry.ioc.internal.util.IdAllocator;

/**
 * Provides support to all components that render. This is primarily about generating unique
 * client-side ids (very important for JavaScript generation) as well as accumulating JavaScript to
 * be sent to the client.
 */
public interface PageRenderSupport
{
    /**
     * Allocates a unique id based on the component's id. In some cases, the return value will not
     * precisely match the input value (an underscore and a unique index value may be appended).
     * 
     * @param id
     *            the component id from which a unique id will be generated
     * @return a unqiue id for this rendering of the page
     * @see IdAllocator
     */
    String allocateClientId(String id);
}
