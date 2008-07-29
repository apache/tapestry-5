// Copyright 2007, 2008 The Apache Software Foundation
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

import java.util.Collection;

/**
 * Scans the classpath for top-level classes within particular packages.
 *
 * @see org.apache.tapestry5.ioc.services.ClasspathURLConverter
 */
public interface ClassNameLocator
{
    /**
     * Searches for all classes under the given package name. This consists of all top-level classes in the indicated
     * package (or any sub-package), but excludes inner classes. No other filtering (beyond inner classes) occurs, so
     * there's no guarantee that the class names returned are public (for example).
     *
     * @param packageName
     * @return fully qualified class names
     */
    Collection<String> locateClassNames(String packageName);
}
