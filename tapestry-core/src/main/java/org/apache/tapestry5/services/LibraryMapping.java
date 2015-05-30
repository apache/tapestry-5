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

import org.apache.tapestry5.ioc.internal.util.InternalUtils;

/**
 * Used to configure the {@link ComponentClassResolver}, to allow it to map library names to library root packages (the
 * application namespace is a special case of this). In each case, a prefix on the path is mapped to a package.
 *
 * The root package name should have a number of sub-packages:
 * <dl>
 * <dt>pages</dt>
 * <dd>contains named pages</dd>
 * <dt>components</dt>
 * <dd>contains components</dd>
 * <dt>mixins</dt>
 * <dd>contains component mixins</dd>
 * <dt>base</dt>
 * <dd>contains base classes</dd>
 * </dl>
 *
 * @see ComponentLibraryInfo 
 */
public final class LibraryMapping
{
    public final String libraryName, rootPackage;
    
    /**
     * Identifies the root package of a library. The application has uses the library name "" (the empty string).
     * The special library "core" is all the built-in components.
     *
     * The library name is sometimes referred to as the "path prefix" or the "virtual folder name". This is for historical
     * reasons, as the concept of a library and how it was defined and managed evolved from release to release.
     *
     * The library name should be alpha numeric, and directly encodable into a URL. It may contain slashes (though this is not
     * used often), but may not start or end with one.
     *
     * Note that it <em>is</em> allowed to contribute multiple LibraryMappings with the library name to the
     * {@link ComponentClassResolver}, and the results are merged: the single library will have multiple root packages.
     * Be careful that <em>none</em> of the root packages overlap!
     *
     * @param libraryName
     *         the unique identifier for the library.
     * @param rootPackage
     *         the root package to search for classes; sub-packages will include ".pages", ".components", etc.
     */
    public LibraryMapping(String libraryName, String rootPackage)
    {
        assert libraryName != null;
        assert InternalUtils.isNonBlank(rootPackage);

        if (libraryName.startsWith("/") || libraryName.endsWith("/"))
        {
            throw new IllegalArgumentException(
                    "Library names may not start with or end with a slash.");
        }

        this.libraryName = libraryName;
        this.rootPackage = rootPackage;
    }

    /**
     * Returns the library name; the method is oddly named for historical reasons. The library name is sometimes
     * referred to as the virtual folder name.
     *
     * @deprecated In 5.4, use {@link #libraryName} instead.
     */
    public String getPathPrefix()
    {
        return libraryName;
    }

    public String getRootPackage()
    {
        return rootPackage;
    }
    
    @Override
    public String toString()
    {
        return String.format("LibraryMapping[%s, %s]", libraryName, rootPackage);
    }
    
}
