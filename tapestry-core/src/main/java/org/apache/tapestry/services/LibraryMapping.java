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

/**
 * Used to configure the {@link ComponentClassResolver}, to allow it to map prefixes to library
 * root packages (the application namespace is a special case of this). In each case, a prefix on
 * the path is mapped to a package. Prefixes should start with a character and end with a slash, as
 * in "core". The root package name should have two sub-packages: "pages" to contain named pages,
 * and "components" to contain named components.
 * 
 * 
 */
public final class LibraryMapping
{
    private final String _pathPrefix;

    private final String _rootPackage;

    public LibraryMapping(String pathPrefix, String rootPackage)
    {
        _pathPrefix = pathPrefix;
        _rootPackage = rootPackage;
    }

    public String getPathPrefix()
    {
        return _pathPrefix;
    }

    public String getRootPackage()
    {
        return _rootPackage;
    }

    @Override
    public String toString()
    {
        return String.format("LibraryMapping[%s, %s]", _pathPrefix, _rootPackage);
    }
}
