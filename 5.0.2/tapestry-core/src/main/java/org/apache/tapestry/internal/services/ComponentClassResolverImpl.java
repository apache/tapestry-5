// Copyright 2006, 2007 The Apache Software Foundation
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

package org.apache.tapestry.internal.services;

import static org.apache.tapestry.ioc.internal.util.CollectionFactory.newCaseInsensitiveMap;
import static org.apache.tapestry.ioc.internal.util.CollectionFactory.newList;
import static org.apache.tapestry.ioc.internal.util.CollectionFactory.newMap;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.tapestry.events.InvalidationListener;
import org.apache.tapestry.services.ComponentClassResolver;
import org.apache.tapestry.services.LibraryMapping;

public class ComponentClassResolverImpl implements ComponentClassResolver, InvalidationListener
{
    private static final String MIXINS_SUBPACKAGE = "mixins";

    private static final String COMPONENTS_SUBPACKAGE = "components";

    private static final String PAGES_SUBPACKAGE = "pages";

    private final ComponentInstantiatorSource _componentInstantiatorSource;

    private final ComponentClassLocator _componentClassLocator;

    private String _appRootPackage;

    // Map from folder name to a list of root package names.
    // The key does not begin or end with a slash.

    private final Map<String, List<String>> _mappings = newCaseInsensitiveMap();

    // Flag indicating that the maps have been cleared following an invalidation
    // and need to be rebuilt. The flag and the four maps below are not synchronized
    // because they are only modified inside a synchronized block. That should be strong enough ...
    // and changes made will become "visible" at the end of the synchronized block. Because of the
    // structure of Tapestry, there should not be any reader threads while the write thread
    // is operating.

    private boolean _needsRebuild = true;

    private final Map<String, String> _pageToClassName = newCaseInsensitiveMap();

    private final Map<String, String> _componentToClassName = newCaseInsensitiveMap();

    private final Map<String, String> _mixinToClassName = newCaseInsensitiveMap();

    /** This one is case sensitive, since class names do always have a particular case. */
    private final Map<String, String> _pageClassNameToLogicalName = newMap();

    public ComponentClassResolverImpl(ComponentInstantiatorSource componentInstantiatorSource,
            ComponentClassLocator componentClassLocator, Collection<LibraryMapping> mappings)
    {
        _componentInstantiatorSource = componentInstantiatorSource;
        _componentClassLocator = componentClassLocator;

        for (LibraryMapping mapping : mappings)
        {
            String prefix = mapping.getPathPrefix();

            // TODO: Check that prefix is well formed (no leading or trailing slash)

            String rootPackage = mapping.getRootPackage();

            List<String> packages = _mappings.get(prefix);

            if (packages == null)
            {
                packages = newList();
                _mappings.put(prefix, packages);
            }

            packages.add(rootPackage);

            // These packages, which will contain classes subject to class transformation,
            // must be registered with the component instantiator (which is responsible
            // for transformation).

            addPackagesToInstantiatorSource(rootPackage);
        }

    }

    private void addPackagesToInstantiatorSource(String rootPackage)
    {
        _componentInstantiatorSource.addPackage(rootPackage + ".pages");
        _componentInstantiatorSource.addPackage(rootPackage + ".components");
        _componentInstantiatorSource.addPackage(rootPackage + ".mixins");
        _componentInstantiatorSource.addPackage(rootPackage + ".base");
    }

    /** When the class loader is invalidated, clear any cached page names or component types. */
    public synchronized void objectWasInvalidated()
    {
        _needsRebuild = true;

        _pageToClassName.clear();
        _componentToClassName.clear();
        _mixinToClassName.clear();
        _pageClassNameToLogicalName.clear();

    }

    private synchronized void rebuild()
    {
        if (!_needsRebuild)
            return;

        rebuild("", _appRootPackage);

        for (String prefix : _mappings.keySet())
        {
            List<String> packages = _mappings.get(prefix);

            String folder = prefix + "/";

            for (String packageName : packages)
                rebuild(folder, packageName);
        }

        _needsRebuild = false;
    }

    private void rebuild(String pathPrefix, String rootPackage)
    {
        fillNameToClassNameMap(pathPrefix, rootPackage, PAGES_SUBPACKAGE, _pageToClassName);
        fillNameToClassNameMap(
                pathPrefix,
                rootPackage,
                COMPONENTS_SUBPACKAGE,
                _componentToClassName);
        fillNameToClassNameMap(pathPrefix, rootPackage, MIXINS_SUBPACKAGE, _mixinToClassName);
    }

    private void fillNameToClassNameMap(String pathPrefix, String rootPackage, String subPackage,
            Map<String, String> logicalNameToClassName)
    {
        String searchPackage = rootPackage + "." + subPackage;
        boolean isPage = subPackage.equals(PAGES_SUBPACKAGE);

        Collection<String> classNames = _componentClassLocator
                .locateComponentClassNames(searchPackage);

        int startPos = searchPackage.length() + 1;

        for (String name : classNames)
        {
            String withinPackage = name.substring(startPos).replace('.', '/');

            String logicalName = pathPrefix + withinPackage;

            if (isPage)
                _pageClassNameToLogicalName.put(name, logicalName);

            logicalNameToClassName.put(logicalName, name);
        }
    }

    public String resolvePageNameToClassName(String pageName)
    {
        String result = locate(pageName, _pageToClassName);

        if (result == null)
            throw new IllegalArgumentException(ServicesMessages.couldNotResolvePageName(
                    pageName,
                    _pageToClassName.keySet()));

        return result;
    }

    public boolean isPageName(String pageName)
    {
        return locate(pageName, _pageToClassName) != null;
    }

    public String resolveComponentTypeToClassName(String componentType)
    {
        String result = locate(componentType, _componentToClassName);

        if (result == null)
            throw new IllegalArgumentException(ServicesMessages.couldNotResolveComponentType(
                    componentType,
                    _componentToClassName.keySet()));

        return result;
    }

    public String resolveMixinTypeToClassName(String mixinType)
    {
        String result = locate(mixinType, _mixinToClassName);

        if (result == null)
            throw new IllegalArgumentException(ServicesMessages.couldNotResolveMixinType(
                    mixinType,
                    _mixinToClassName.keySet()));

        return result;
    }

    /**
     * Locates a class name within the provided map, given its logical name. If not found naturally,
     * a search inside the "core" library is included.
     * 
     * @param logicalName
     *            name to search for
     * @param logicalNameToClassName
     *            mapping from logical name to class name
     * @return the located class name or null
     */
    private synchronized String locate(String logicalName,
            Map<String, String> logicalNameToClassName)
    {
        rebuild();

        String result = logicalNameToClassName.get(logicalName);

        // If not found, see if it exists under the core package. In this way,
        // anything in core is "inherited" (but overridable) by the application.

        if (result == null)
            result = logicalNameToClassName.get("core/" + logicalName);

        return result;
    }

    public synchronized String resolvePageClassNameToPageName(String pageClassName)
    {
        rebuild();

        String result = _pageClassNameToLogicalName.get(pageClassName);

        if (result == null)
            throw new IllegalArgumentException(ServicesMessages.pageNameUnresolved(pageClassName));

        return result;
    }

    public void setApplicationPackage(String packageName)
    {
        _appRootPackage = packageName;

        addPackagesToInstantiatorSource(packageName);
    }
}