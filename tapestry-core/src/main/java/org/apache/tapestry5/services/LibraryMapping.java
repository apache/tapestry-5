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

package org.apache.tapestry5.services;

/**
 * Used to configure the {@link ComponentClassResolver}, to allow it to map prefixes to library root packages (the
 * application namespace is a special case of this). In each case, a prefix on the path is mapped to a package. Prefixes
 * should start and end with characters, such as "core". It is allowed for a prefix to contain a slash, though it is not
 * recommended.
 * <p/>
 * The root package name should have a number of sub-packages: <dl> <dt>pages</dt> <dd>contains named pages</dd>
 * <dt>components</dt> <dd>contains components</dd> <dt>mixins</dt> <dd>contains component mixins</dd> <dt>base</dt>
 * <dd>contains base classes</dd> </dl>
 *
 * @see org.apache.tapestry5.services.TapestryModule#contributeComponentClassResolver(org.apache.tapestry5.ioc.Configuration)
 */
public final class LibraryMapping
{
    private final String pathPrefix, rootPackage;

    public LibraryMapping(String pathPrefix, String rootPackage)
    {
        this.pathPrefix = pathPrefix;
        this.rootPackage = rootPackage;
    }

    public String getPathPrefix()
    {
        return pathPrefix;
    }

    public String getRootPackage()
    {
        return rootPackage;
    }

    @Override
    public String toString()
    {
        return String.format("LibraryMapping[%s, %s]", pathPrefix, rootPackage);
    }
}
