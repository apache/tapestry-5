// Copyright 2006, 2008 The Apache Software Foundation
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

import org.apache.tapestry5.ioc.annotations.UsesMappedConfiguration;

/**
 * Used as part of the support for classpath {@link org.apache.tapestry5.Asset}s, to convert the Asset's {@link
 * org.apache.tapestry5.ioc.Resource} to a URL that can be accessed by the client.    The asset path, within the
 * classpath, is converted into a shorter virtual path (one that, typically, includes some kind of version number).
 * <p/>
 * Service configuration is a map from aliases (short names) to complete names. Names should not start or end end with a
 * slash.
 */
@UsesMappedConfiguration(String.class)
public interface ClasspathAssetAliasManager
{
    /**
     * Takes a resource path to a classpath resource and adds the asset path prefix to the path. May also convert part
     * of the path to an alias (based on the manager's configuration).
     *
     * @param resourcePath resource path on the classpath (with no leading slash)
     * @return URL ready to send to the client
     */
    String toClientURL(String resourcePath);

    /**
     * Reverses {@link #toClientURL(String)}, stripping off the asset prefix, and re-expanding any aliased folders back
     * to complete folders.
     */
    String toResourcePath(String clientURL);
}
