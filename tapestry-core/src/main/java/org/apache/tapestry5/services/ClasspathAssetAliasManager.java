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

import org.apache.tapestry5.commons.Resource;
import org.apache.tapestry5.internal.services.assets.ClasspathAssetRequestHandler;
import org.apache.tapestry5.ioc.annotations.IncompatibleChange;
import org.apache.tapestry5.ioc.annotations.UsesMappedConfiguration;

import java.util.Map;

/**
 * Used as part of the support for classpath {@link org.apache.tapestry5.Asset}s, to convert the Asset's
 * {@link org.apache.tapestry5.commons.Resource} to a URL that can be accessed by the client. The asset path, within the
 * classpath, is converted into a shorter virtual path. The term "alias" here is generally referred to as
 * "virtual folder" elsewhere.
 *
 * Service configuration is a map from folder aliases (short names) to complete paths. Names should not start or end end
 * with a slash. Generally, an alias should be a single name (not contain a slash). Paths should also not start or end
 * with a slash. An example mapping would be <code>mylib</code> to <code>com/example/mylib</code>.
 *
 * As originally envisioned, this service would simply <em>optimize</em> classpath assets, allowing the URL path for
 * such assets to be shortened (and have a version number added, important for caching); thus the word "alias" makes
 * sense ... it was responsible for creating an "alias" URL shorter than the default "classpath" URL.
 *
 * Starting in Tapestry 5.2, this changed; all classpath assets <strong>must</strong> be "aliased" to a shorter URL
 * path. Any URL that can not be shortened is now rejected. This simplifies creating new libraries, but also helps with
 * security concerns, as it limits which portions of the classpath can <em>ever</em> be exposed to the user agent.
 *
 * Tapestry automatically contributes a number of mappings: for the application root package itself (as alias "app") and
 * for each library (via {@link ComponentClassResolver#getFolderToPackageMapping()});
 *
 * @deprecated Deprecated in 5.4, with no replacement. This will no longer be used in Tapestry 5.5, as all classpath assets
 *             will need to be under the {@code META-INF/assets} folder (but may be maintained for compatibility reasons until 5.6).
 */
@UsesMappedConfiguration(String.class)
public interface ClasspathAssetAliasManager
{
    /**
     * Takes a classpath resource and determines the proper alias for it based on the mappings contributed to the service.
     *
     * @param resource
     *         classpath resource
     * @return URL ready to send to the client
     */
    @IncompatibleChange(release = "5.4", details = "parameter changed from String to Resource, renamed from toClientURL() to better identify purpose")
    AssetAlias extractAssetAlias(Resource resource);

    /**
     * Returns the mappings used by the service: the keys are the folder aliases (i.e, "corelib")
     * and the values are the corresponding paths (i.e., "org/apache/tapestry5/corelib"). This
     * exists primarily so that {@link ClasspathAssetRequestHandler}s can be created automatically
     * for each mapping.
     *
     * @since 5.2.0
     */
    Map<String, String> getMappings();
}
