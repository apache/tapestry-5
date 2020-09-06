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
import org.apache.tapestry5.ioc.annotations.IncompatibleChange;
import org.apache.tapestry5.ioc.annotations.UsesConfiguration;
import org.apache.tapestry5.ioc.services.ClassNameLocator;
import org.apache.tapestry5.services.transform.ControlledPackageType;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Resolves page names and component types to fully qualified class names. Pages and components may be provided by the
 * application or inside a <em>mapped package</em>. Page names often appear inside URLs, and component types often
 * appear in component templates (when specifying the type of an embedded component).
 *
 * The service is configured using a collection of {@link LibraryMapping}s. Each mapping maps a prefix, such as "core"
 * to a root package name, such as "org.apache.tapestry5.corelib". The root package is expected to have sub-packages:
 * "pages", "components", "mixins" and "base" ("base" is for base classes).
 *
 * The resolver performs a search of the classpath (via {@link ClassNameLocator}), to build up a set of case-insensitive
 * maps from logical page name, component type, or mixin type to fully qualified class name.
 *
 * Certain ambiguities occur if mapped packages overlap, either in terms of the the prefixes or the package names. Keep
 * things clearly separate to avoid lookup problems.
 */
@UsesConfiguration(LibraryMapping.class)
public interface ComponentClassResolver
{
    /**
     * Converts a logical page name (such as might be encoded into a URL) into a fully qualified class name. The case of
     * the page name is irrelevant.
     *
     * @param pageName
     *         page name
     * @return fully qualified class name for the page
     * @throws org.apache.tapestry5.commons.util.UnknownValueException
     *         if the name does not match a known page class
     */
    String resolvePageNameToClassName(String pageName);

    /**
     * For a particular path, determines if the path is a logical page name. The check is case insensitive.
     *
     * @param pageName
     *         potential logical page name
     * @return true if the page name is valid
     */
    boolean isPageName(String pageName);

    /**
     * Returns a list of all page names, in sorted order. These are the "canonical" page names.
     */
    List<String> getPageNames();

    /**
     * Returns a list of all component names, in sorted order. These are the "canonical" component names.
     * @since 5.4
     */
    @IncompatibleChange(release = "5.4", details = "added method")
    List<String> getComponentNames();

    /**
     * Returns a list of all mixin names, in sorted order. These are the "canonical" mixin names.
     * @since 5.4
     */
    @IncompatibleChange(release = "5.4", details = "added method")
    List<String> getMixinNames();
    
    /**
     * Converts a fully qualified page class name into a page name (often, for inclusion as part of the URI). This value
     * may later be passed to {@link #resolvePageNameToClassName(String)}.
     *
     * @param pageClassName
     *         fully qualified name of a page class
     * @return equivalent logical page name
     * @throws IllegalArgumentException
     *         if the name can not be resolved
     */
    String resolvePageClassNameToPageName(String pageClassName);

    /**
     * Returns the canonical form of a page name. The canonical form uses character case matching the underlying class
     * name.
     *
     * @throws org.apache.tapestry5.commons.util.UnknownValueException
     *         if the page name does not match a logical page name
     */
    String canonicalizePageName(String pageName);

    /**
     * Converts a component type (a logical component name such as might be used inside a template or annotation) into a
     * fully qualified class name. Case is ignored in resolving the name.
     *
     * @param componentType
     *         a logical component type
     * @return fully qualified class name
     * @throws org.apache.tapestry5.commons.util.UnknownValueException
     *         if the component type can not be resolved
     */
    String resolveComponentTypeToClassName(String componentType);

    /**
     * Converts a logical mixin type (as with component types) into a fully qualified class name. Case is ignored when
     * resolving the name.
     *
     * @param mixinType
     *         a logical mixin type
     * @return fully qualified class name
     * @throws org.apache.tapestry5.commons.util.UnknownValueException
     *         if the mixin type can not be resolved
     */
    String resolveMixinTypeToClassName(String mixinType);

    /**
     * A mapping from virtual folder name to a package name (used for converting classpath {@link Asset}s
     * to client URLs). This is derived from the contributed {@link LibraryMapping}s.
     *
     * It is allowed to contribute multiple root packages as a single folder name. In this case, the best common package
     * name is used. For example, if both <code>com.example.main</code> and <code>com.example.extras</code> is mapped to
     * folder "example", then the package mapping for "example" will be <code>com.example</code>.
     *
     * @see ClasspathAssetAliasManager
     * @since 5.2.0
     */
    Map<String, String> getFolderToPackageMapping();

    /**
     * Returns the names of all libraries (as {@linkplain org.apache.tapestry5.services.LibraryMapping#getPathPrefix() configured}).
     * This does not include the application itself (which is a library with the virtual path of empty string).
     *
     * @return sorted names of libraries
     * @since 5.4
     */
    List<String> getLibraryNames();
    
    /**
     * Used to identify which packages are controlled packages (from which components are loaded). Future expansion
     * may allow for additional packages which are live reloaded but not components (or perhaps are transformed, but not
     * as components).
     *
     * @return a mapping from package name to {@link ControlledPackageType}.
     * @since 5.3
     */
    Map<String, ControlledPackageType> getControlledPackageMapping();

    /**
     * Returns true if the class name is specifically a page class, and not a component, mixin or base class.
     *
     * @param pageClassName
     * @return true if a page class
     * @since 5.3
     */
    boolean isPage(final String pageClassName);

    /**
     * Given a class name of a component class, returns the library name (as defined by a
     * {@linkplain org.apache.tapestry5.services.LibraryMapping#getPathPrefix() contributed library name}).
     *
     * @param className
     * @return library name
     * @throws IllegalArgumentException
     *         if the class can't be matched to a contributed root package
     */
    String getLibraryNameForClass(String className);
    
    /**
     * Returns the library mappings.
     * @return
     */
    @IncompatibleChange(release = "5.4", details = "Added method")
    Collection<LibraryMapping> getLibraryMappings();
    
}
