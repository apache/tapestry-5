// Copyright 2013 The Apache Software Foundation
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

package org.apache.tapestry5.services;

import org.apache.tapestry5.http.services.Dispatcher;

/**
 * Central location for logic related to building client-side paths, taking into account
 * the context path (if any), and the {@link org.apache.tapestry5.SymbolConstants#APPLICATION_FOLDER}
 * (if any).
 *
 * @since 5.4
 */
public interface PathConstructor
{
    /**
     * Constructs a client path, the path portion of an absolute URL. The result consists of the
     * the context path (if any), the application folder (if any), then the series of terms.
     *
     * @param terms
     *         additional terms (folder names, or a file name) following the context path and application folder.
     * @return the full path, starting with a leading slash, and including the context path, application folder, and the terms,
     *         all seperated with slashes
     */
    String constructClientPath(String... terms);

    /**
     * Constructs the dispatch path, which is like the client path, but omits the context path; this aligns
     * the result with the value returned from {@link org.apache.tapestry5.http.services.Request#getPath()}, and is used
     * in code, typically {@link Dispatcher} implementations, that are attempting to route based on the incoming request path.
     *
     * @param terms
     *         additional terms (folder names, or a file name) following the context path and application folder.
     * @return path string starting with a leading slash, and including the application folder (if any) and the individual terms,
     *         seperated by slashes
     */
    String constructDispatchPath(String... terms);
}
