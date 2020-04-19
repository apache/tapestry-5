// Copyright 2012 The Apache Software Foundation
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

import java.io.IOException;
import java.util.Set;

/**
 * Used to scan a portion of the classpath for files that match a particular pattern, defined by a {@link ClasspathMatcher}.
 *
 * @since 5.4
 */
public interface ClasspathScanner
{
    /**
     * Perform a scan of the indicated package path and any nested packages.
     *
     * @param packagePath
     *         defines the root of the search as a path, e.g., "org/apache/tapestry5/" not "org.apache.tapestry5"
     * @param matcher
     *         passed each potential match to determine which are included in the final result
     * @return matching paths based on the search and the matcher
     * @throws IOException if some error occurrs.
     */
    Set<String> scan(String packagePath, ClasspathMatcher matcher) throws IOException;
}
