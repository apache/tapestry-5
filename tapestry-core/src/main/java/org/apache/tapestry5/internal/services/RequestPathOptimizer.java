// Copyright 2008 The Apache Software Foundation
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

package org.apache.tapestry5.internal.services;

/**
 * Used to optimize a path for inclusion in the rendered output of the page. When using lots of libraries, nested
 * folders, and page acivation contexts, you can often create a shorter URL as a relative path from the current request
 * URL.  Of course, you need to make sure that's turned off inside an Ajax request since the base URL of the client it
 * totally unknown in that situation.
 *
 * @see org.apache.tapestry5.SymbolConstants#FORCE_ABSOLUTE_URIS
 */
public interface RequestPathOptimizer
{
    /**
     * Optimizes the provided path, returning a new path that is shorter but (combined with the current requests' base
     * URL) will result in the same request URI.  In many cases, this will return the provided path unchanged. During
     * {@linkplain org.apache.tapestry5.services.Request#isXHR() XHR} requests, this will always return the provided
     * path (no optimization takes place, since the base URI of the client is unknown).
     *
     * @param path to be optimized
     * @return the same path, or a new path that is equivalent, relative to the current request's URL
     */
    String optimizePath(String path);
}
