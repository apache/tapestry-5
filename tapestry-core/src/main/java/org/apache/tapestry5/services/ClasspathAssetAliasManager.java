// Copyright 2006, 2008, 2010, 2011 The Apache Software Foundation
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

import java.util.Map;

import org.apache.tapestry5.internal.services.assets.ClasspathAssetRequestHandler;
import org.apache.tapestry5.ioc.annotations.UsesMappedConfiguration;

/**
 * Used as part of the support for classpath {@link org.apache.tapestry5.Asset}s, to convert the Asset's
 * {@link org.apache.tapestry5.ioc.Resource} to a URL that can be accessed by the client. The asset path, within the
 * classpath, is converted into a shorter virtual path. The term "alias" here is generally referred to as
 * "virtual folder" elsewhere.
 * <p/>
 * Service configuration is a map from folder aliases (short names) to complete paths. Names should not start or end end
 * with a slash. Generally, an alias should be a single name (not contain a slash). Paths should also not start or end
 * with a slash. An example mapping would be <code>mylib</code> to <code>com/example/mylib</code>.
 * <p>
 * As originally envisioned, this service would simply <em>optimize</em> classpath assets, allowing the URL path for
 * such assets to be shortened (and have a version number added, important for caching); thus the word "alias" makes
 * sense ... it was responsible for creating an "alias" URL shorter than the default "classpath" URL.
 * <p>
 * Starting in Tapestry 5.2, this changed; all classpath assets <strong>must</strong> be "aliased" to a shorter URL
 * path. Any URL that can not be shortened is now rejected. This simplifies creating new libraries, but also helps with
 * security concerns, as it limits which portions of the classpath can <em>ever</em> be exposed to the user agent.
 * <p>
 * Tapestry automatically contributes a number of mappings: for the application root package itself (as alias "app") and
 * for each library (via {@link ComponentClassResolver#getFolderToPackageMapping()});
 */
@UsesMappedConfiguration(String.class)
public interface ClasspathAssetAliasManager
{
    /**
     * Takes a resource path to a classpath resource and adds the asset path prefix to the path. May also convert part
     * of the path to an alias (based on the manager's configuration).
     * 
     * @param resourcePath
     *            resource path on the classpath (with no leading slash)
     * @return URL ready to send to the client
     */
    String toClientURL(String resourcePath);

    /**
     * Returns the mappings used by the service: the keys are the folder aliases (i.e, "corelib")
     * and the values are the corresponding paths (i.e., "org/apache/tapestry5/corelib"). This
     * exists primarily so that {@link ClasspathAssetRequestHandler}s can be created automatically
     * for each mapping.
     * 
     * @since 5.2.0
     **/
    Map<String, String> getMappings();
}
