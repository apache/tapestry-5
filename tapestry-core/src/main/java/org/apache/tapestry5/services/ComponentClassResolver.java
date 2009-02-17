// Copyright 2006, 2007, 2008, 2009 The Apache Software Foundation
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

import org.apache.tapestry5.ioc.services.ClassNameLocator;

import java.util.List;

/**
 * Resolves page names and component types to fully qualified class names. Pages and components may be provided by the
 * application or inside a <em>mapped package</em>. Page names often appear inside URLs, and component types often
 * appear in component template (when specifying the type of an embedded component).
 * <p/>
 * The service is configured using a collection of {@link LibraryMapping}s. Each mapping maps a prefix, such as "core"
 * to a root package name, such as "org.apache.tapestry5.corelib". The root package is expected to have sub-packages:
 * "pages", "components", "mixins" and "base" ("base" is for base classes).
 * <p/>
 * The resolver performs a search of the classpath (via {@link ClassNameLocator}), to build up a set of case-insensitive
 * maps from logical page name, component type, or mixin type to fully qualified class name.
 * <p/>
 * Certain ambiguities occur if mapped packages overlap, either in terms of the the prefixes or the package names. Keep
 * things clearly seperate to avoid lookup problems.
 */
public interface ComponentClassResolver
{
    /**
     * Converts a logical page name (such as might be encoded into a URL) into a fully qualified class name. The case of
     * the page name is irrelevant.
     *
     * @param pageName page name
     * @return fully qualified class name for the page
     * @throws IllegalArgumentException if the name does not match a known page class
     */
    String resolvePageNameToClassName(String pageName);

    /**
     * For a particular path, determines if the path is a logical page name. The check is case insensitive.
     *
     * @param pageName potential logical page name
     * @return true if the page name is valid
     */
    boolean isPageName(String pageName);

    /**
     * Returns a list of all  page names, in sorted order.
     */
    List<String> getPageNames();

    /**
     * Converts a fully qualified page class name into a page name (often, for inclusion as part of the URI). This value
     * may later be passed to {@link #resolvePageNameToClassName(String)}.
     *
     * @param pageClassName fully qualified name of a page class
     * @return equivalent logical page name
     * @throws IllegalArgumentException if the name can not be resolved
     */
    String resolvePageClassNameToPageName(String pageClassName);

    /**
     * Returns the canonical form of a page name. The canonical form uses character case matching the underlying class
     * name.
     *
     * @throws IllegalArgumentException if the page name does not match a logical page name
     */
    String canonicalizePageName(String pageName);

    /**
     * Converts a component type (a logical component name such as might be used inside a template or annotation) into a
     * fully qualified class name. Case is ignored in resolving the name.
     *
     * @param componentType a logical component type
     * @return fully qualified class name
     * @throws IllegalArgumentException if the component type can not be resolved
     */
    String resolveComponentTypeToClassName(String componentType);

    /**
     * Converts a logical mixin type (as with component types) into a fully qualified class name. Case is ignored when
     * resolving the name.
     *
     * @param mixinType a logical mixin type
     * @return fully qualified class name
     * @throws IllegalArgumentException if the mixin type can not be resolved
     */
    String resolveMixinTypeToClassName(String mixinType);
}
