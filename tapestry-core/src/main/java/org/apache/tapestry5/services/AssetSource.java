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

import org.apache.tapestry5.Asset;
import org.apache.tapestry5.ComponentResources;
import org.apache.tapestry5.commons.Resource;
import org.apache.tapestry5.ioc.annotations.UsesMappedConfiguration;
import org.apache.tapestry5.ioc.services.SymbolSource;
import org.apache.tapestry5.ioc.services.ThreadLocale;

import java.util.Locale;

/**
 * Used to find or create an {@link org.apache.tapestry5.Asset} with a given path.
 *
 * Assets are defined with a domain, and the domain is indicated by a prefix. The two builtin domains are "context:"
 * (for files inside the web application context) and "classpath:" for files stored on the classpath (typically, inside
 * a JAR, such as a component library). Other domains can be defined via contributions to the AssetSource service.
 *
 * Since 5.1.0.0, is is preferred that
 * {@link org.apache.tapestry5.services.AssetFactory#createAsset(org.apache.tapestry5.commons.Resource)} return an instance
 * of <code>org.apache.tapestry5.Asset2</code>.
 * 
 * Since 5.7.0, Asset2 was merged into Asset and removed.
 */
@UsesMappedConfiguration(AssetFactory.class)
public interface AssetSource
{
    /**
     * Finds the asset. The path may either be a simple file name or a relative path (relative to the base resource)
     * <em>or</em> it may have a prefix, such as "context:" or "classpath:", in which case it is treated as a complete
     * path within the indicated domain. The resulting Resource is then localized (to the provided Locale) and returned
     * as an Asset.
     *
     * The AssetSource caches its results, so a single Asset instance may be shared among many different components.
     *
     * @param baseResource
     *         base resource for computing relative paths, or null to search the classpath
     * @param path
     *         relative to the base resource
     * @param locale
     *         locale to localize the final resource to, or null for the thread's current locale
     * @return the asset
     * @throws RuntimeException
     *         if the asset can not be found
     */
    Asset getAsset(Resource baseResource, String path, Locale locale);

    /**
     * Finds the asset, either on the classpath or (if prefixed), within the indicated domain. The result is not
     * localized. The underlying Asset may not exist.
     *
     * @param path
     *         to the resource to provide as an Asset
     * @return Resource for the path (the Resource may not exist)
     * @since 5.1.0.0
     */
    Resource resourceForPath(String path);

    /**
     * Convenience for finding assets on the classpath.
     *
     * @param path
     *         path to the base resource, relative to classpath root
     * @param locale
     *         to localize the resource to
     * @return the asset
     * @throws RuntimeException
     *         if the asset can not be found
     */
    Asset getClasspathAsset(String path, Locale locale);

    /**
     * Convenience for finding assets in the context.
     *
     * @param path
     *         path relative to the base resource (the context root)
     * @param locale
     *         to localize the resource to, or null for the locale for the current request
     * @return the asset
     * @throws RuntimeException
     *         if the asset can not be found
     * @since 5.1.0.0
     */
    Asset getContextAsset(String path, Locale locale);

    /**
     * Obtains a classpath asset in the current locale (as defined by the {@link ThreadLocale} service).
     *
     * @param path
     *         relative to the classpath root
     * @return the asset
     * @throws RuntimeException
     *         if the asset can not be found
     */
    Asset getClasspathAsset(String path);

    /**
     * Find an asset but does not attempt to localize it. If the path has no prefix, it is assumed to
     * be on the classpath.
     *
     * @throws RuntimeException
     *         if the asset can not be found
     * @since 5.2.0
     */
    Asset getUnlocalizedAsset(String path);

    /**
     * As with {@link #getUnlocalizedAsset(String)}, but {@linkplain SymbolSource#expandSymbols(String) symbols}
     * in the path are expanded}.
     *
     * @since 5.2.0
     */
    Asset getExpandedAsset(String path);

    /**
     * Gets an asset that is used with, or injected into, a component, that will be exposed to the client.
     * This encapsulates the new, in 5.4, standard that assets should all be stored in (sub-folders of)
     * <code>META-INF/assets/<em>library-name</em>/</code>.
     * This is the preferred location in 5.4, with compatibility for 5.3 that allows assets to be stored on the classpath
     * alongside Java classes and server-only resources such as templates and message catalogs.
     *
     *
     * When resolving a resource in a component that is subclass, the point of injection is the class which contains
     * the injecting annotation (e.g., {@link org.apache.tapestry5.ioc.annotations.Inject} with {@link org.apache.tapestry5.annotations.Path},
     * or {@link org.apache.tapestry5.annotations.Import}). In other words, the library name for the library containing the class,
     * rather than the library name of the instantiated subclass (which can be different).
     *
     * @param resources
     *         resources, used to identify starting location of asset (if path does not include a asset prefix).
     * @param path
     *         path to the resource; either fully qualified (with an asset prefix such as "context:"), or relative to the
     *         component's library asset folder (the 5.4 and beyond way), or the to the component's Java class file (the 5.3 and earlier
     *         way, still supported until at least 5.5).
     *         Symbols in the path are {@linkplain org.apache.tapestry5.ioc.services.SymbolSource#expandSymbols(String) expanded}.
     * @param libraryName
     *          The name of the library containing the component, as per {@link org.apache.tapestry5.model.ComponentModel#getLibraryName()}.
     *          For a subclass, the libraryName must reflect the name of the library for the parent class that forms the basis of
     *          injection.
     * @return the Asset
     * @throws RuntimeException
     *         if Asset can not be found
     * @since 5.4
     */
    Asset getComponentAsset(ComponentResources resources, String path, final String libraryName);
}
