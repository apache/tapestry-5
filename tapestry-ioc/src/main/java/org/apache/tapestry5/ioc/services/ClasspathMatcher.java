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

/**
 * Used to determine which files will be included in the set of matches paths within a particular
 * package.
 *
 * @see ClasspathScanner
 * @since 5.4
 */
public interface ClasspathMatcher
{
    /**
     * Invoked for each located file, to determine if it belongs. May be passed file names
     * that are actually nested folders. Typically, an implementation determined what matches
     * based on a file extension of naming pattern.
     *
     * @param packagePath
     *         package path containing the file, ending with '/'
     * @param fileName
     *         name of file within the package
     * @return true to include, false to exclude
     */
    boolean matches(String packagePath, String fileName);
}
