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

import org.apache.tapestry.internal.InternalConstants;
import org.apache.tapestry.internal.events.InvalidationListener;
import org.apache.tapestry.ioc.annotations.Inject;
import org.apache.tapestry.ioc.annotations.Symbol;
import org.apache.tapestry.ioc.internal.util.CollectionFactory;
import static org.apache.tapestry.ioc.internal.util.CollectionFactory.*;
import org.apache.tapestry.ioc.internal.util.ConcurrentBarrier;
import org.apache.tapestry.ioc.internal.util.InternalUtils;
import org.apache.tapestry.ioc.internal.util.Invokable;
import org.apache.tapestry.services.ComponentClassResolver;
import org.apache.tapestry.services.LibraryMapping;
import org.slf4j.Logger;

import java.util.*;

public class ComponentClassResolverImpl implements ComponentClassResolver, InvalidationListener
{
    private static final String CORE_LIBRARY_PREFIX = "core/";

    private static final String MIXINS_SUBPACKAGE = "mixins";

    private static final String COMPONENTS_SUBPACKAGE = "components";

    private static final String PAGES_SUBPACKAGE = "pages";

    private final Logger _logger;

    private final ComponentInstantiatorSource _componentInstantiatorSource;

    private final ClassNameLocator _classNameLocator;

    private final String _appRootPackage;

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

    /**
     * Logical page name to class name.
     */
    private final Map<String, String> _pageToClassName = newCaseInsensitiveMap();

    /**
     * Component type to class name.
     */
    private final Map<String, String> _componentToClassName = newCaseInsensitiveMap();

    /**
     * Mixing type to class name.
     */
    private final Map<String, String> _mixinToClassName = newCaseInsensitiveMap();

    /**
     * Page class name to logical name (needed to build URLs). This one is case sensitive, since class names do always have a particular case.
     */
    private final Map<String, String> _pageClassNameToLogicalName = newMap();


    /**
     * Used to convert an logical page name to the canonical form of the page name; this ensures that uniform case for page
     * names is used.
     */
    private final Map<String, String> _pageNameToCanonicalPageName = newCaseInsensitiveMap();

    private final ConcurrentBarrier _barrier = new ConcurrentBarrier();

    public ComponentClassResolverImpl(Logger logger,

                                      ComponentInstantiatorSource componentInstantiatorSource,

                                      ClassNameLocator classNameLocator,

                                      @Inject @Symbol(InternalConstants.TAPESTRY_APP_PACKAGE_PARAM)
                                      String appRootPackage,

                                      Collection<LibraryMapping> mappings)
    {
        _logger = logger;
        _componentInstantiatorSource = componentInstantiatorSource;
        _classNameLocator = classNameLocator;

        _appRootPackage = appRootPackage;

        addPackagesToInstantiatorSource(_appRootPackage);

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

    /**
     * When the class loader is invalidated, clear any cached page names or component types.
     */
    public synchronized void objectWasInvalidated()
    {
        _barrier.withWrite(new Runnable()
        {
            public void run()
            {
                _needsRebuild = true;
            }
        });

    }

    /**
     * Invoked from within a withRead() block, checks to see if a rebuild is needed,
     * and then performs the rebuild within a withWrite() block.
     */
    private void rebuild()
    {
        if (!_needsRebuild) return;

        _barrier.withWrite(new Runnable()
        {
            public void run()
            {
                performRebuild();
            }
        });
    }

    private void performRebuild()
    {

        Map<String, String> savedPages = CollectionFactory.newMap(_pageToClassName);
        Map<String, String> savedComponents = CollectionFactory.newMap(_componentToClassName);
        Map<String, String> savedMixins = CollectionFactory.newMap(_mixinToClassName);

        _pageToClassName.clear();
        _componentToClassName.clear();
        _mixinToClassName.clear();
        _pageClassNameToLogicalName.clear();
        _pageNameToCanonicalPageName.clear();

        rebuild("", _appRootPackage);

        for (String prefix : _mappings.keySet())
        {
            List<String> packages = _mappings.get(prefix);

            String folder = prefix + "/";

            for (String packageName : packages)
                rebuild(folder, packageName);
        }


        showChanges("pages", savedPages, _pageToClassName);
        showChanges("components", savedComponents, _componentToClassName);
        showChanges("mixins", savedMixins, _mixinToClassName);

        _needsRebuild = false;
    }

    private void showChanges(String title, Map<String, String> savedMap, Map<String, String> newMap)
    {
        if (savedMap.equals(newMap)) return;

        Map<String, String> fixed = CollectionFactory.newMap();

        int maxLength = 0;

        // Pass # 1: Get all the stuff in the core library

        for (String name : newMap.keySet())
        {
            if (name.startsWith(CORE_LIBRARY_PREFIX))
            {

                // Strip of the "core/" prefix.

                String key = name.substring(CORE_LIBRARY_PREFIX.length());

                maxLength = Math.max(maxLength, key.length());

                fixed.put(key, newMap.get(name));
            }
        }

        // Pass #2: Get everything else (may overwrite some of  pass #1).

        for (String name : newMap.keySet())
        {
            if (name.startsWith(CORE_LIBRARY_PREFIX)) continue;

            maxLength = Math.max(maxLength, name.length());

            fixed.put(name, newMap.get(name));
        }


        StringBuilder builder = new StringBuilder(2000);
        Formatter f = new Formatter(builder);

        f.format("Available %s:\n", title);

        String formatString = "%" + maxLength + "s: %s\n";

        List<String> sorted = InternalUtils.sortedKeys(fixed);

        for (String name : sorted)
        {
            String className = fixed.get(name);

            f.format(formatString, name, className);
        }

        _logger.info(builder.toString());

    }

    private void rebuild(String pathPrefix, String rootPackage)
    {
        fillNameToClassNameMap(pathPrefix, rootPackage, PAGES_SUBPACKAGE, _pageToClassName);
        fillNameToClassNameMap(pathPrefix, rootPackage, COMPONENTS_SUBPACKAGE, _componentToClassName);
        fillNameToClassNameMap(pathPrefix, rootPackage, MIXINS_SUBPACKAGE, _mixinToClassName);
    }

    private void fillNameToClassNameMap(String pathPrefix, String rootPackage, String subPackage,
                                        Map<String, String> logicalNameToClassName)
    {
        String searchPackage = rootPackage + "." + subPackage;
        boolean isPage = subPackage.equals(PAGES_SUBPACKAGE);

        Collection<String> classNames = _classNameLocator.locateClassNames(searchPackage);

        int startPos = searchPackage.length() + 1;

        for (String name : classNames)
        {
            String logicalName = toLogicalName(name, pathPrefix, startPos);

            if (isPage)
            {
                _pageClassNameToLogicalName.put(name, logicalName);
                _pageNameToCanonicalPageName.put(logicalName, logicalName);
            }

            logicalNameToClassName.put(logicalName, name);
        }
    }

    /**
     * Converts a fully qualified class name to a logical name
     *
     * @param className  fully qualified class name
     * @param pathPrefix prefix to be placed on the logical name (to identify the library from in which the
     *                   class lives)
     * @param startPos   start position within the class name to extract the logical name (i.e., after the
     *                   final '.' in "rootpackage.pages.").
     * @return a short logical name in folder format ('.' replaced with '/')
     */
    private String toLogicalName(String className, String pathPrefix, int startPos)
    {
        String[] terms = className.substring(startPos).split("\\.");
        StringBuilder builder = new StringBuilder(pathPrefix);
        String sep = "";

        String logicalName = terms[terms.length - 1];

        for (int i = 0; i < terms.length - 1; i++)
        {

            String term = terms[i];

            builder.append(sep);
            builder.append(term);

            sep = "/";

            logicalName = stripTerm(term, logicalName);
        }

        // The problem here is that you can eventually end up with the empty string.

        assert logicalName.length() > 0;

        builder.append(sep);
        builder.append(logicalName);

        return builder.toString();
    }

    private String stripTerm(String term, String logicalName)
    {
        if (isCaselessPrefix(term, logicalName))
        {
            logicalName = logicalName.substring(term.length());
        }

        if (isCaselessSuffix(term, logicalName))
        {
            logicalName = logicalName.substring(0, logicalName.length() - term.length());
        }

        return logicalName;
    }

    private boolean isCaselessPrefix(String prefix, String string)
    {
        return string.regionMatches(true, 0, prefix, 0, prefix.length());
    }

    private boolean isCaselessSuffix(String suffix, String string)
    {
        return string.regionMatches(true, string.length() - suffix.length(), suffix, 0, suffix
                .length());
    }

    public String resolvePageNameToClassName(final String pageName)
    {
        return _barrier.withRead(new Invokable<String>()
        {
            public String invoke()
            {
                String result = locate(pageName, _pageToClassName);

                if (result == null) throw new IllegalArgumentException(
                        ServicesMessages.couldNotResolvePageName(pageName, presentableNames(_pageToClassName)));

                return result;
            }
        });

    }

    public boolean isPageName(final String pageName)
    {
        return _barrier.withRead(new Invokable<Boolean>()
        {
            public Boolean invoke()
            {
                return locate(pageName, _pageToClassName) != null;
            }
        });
    }

    public String resolveComponentTypeToClassName(final String componentType)
    {
        return _barrier.withRead(new Invokable<String>()
        {
            public String invoke()
            {
                String result = locate(componentType, _componentToClassName);

                if (result == null) throw new IllegalArgumentException(ServicesMessages
                        .couldNotResolveComponentType(componentType, presentableNames(_componentToClassName)));

                return result;
            }
        });
    }

    Collection<String> presentableNames(Map<String, ?> map)
    {
        Set<String> result = newSet();

        for (String name : map.keySet())
        {

            if (name.startsWith(CORE_LIBRARY_PREFIX))
            {
                result.add(name.substring(CORE_LIBRARY_PREFIX.length()));
                continue;
            }

            result.add(name);
        }

        return result;
    }

    public String resolveMixinTypeToClassName(final String mixinType)
    {
        return _barrier.withRead(new Invokable<String>()
        {
            public String invoke()
            {
                String result = locate(mixinType, _mixinToClassName);

                if (result == null) throw new IllegalArgumentException(
                        ServicesMessages.couldNotResolveMixinType(mixinType, presentableNames(_mixinToClassName)));

                return result;
            }
        });
    }

    /**
     * Locates a class name within the provided map, given its logical name. If not found naturally,
     * a search inside the "core" library is included.
     *
     * @param logicalName            name to search for
     * @param logicalNameToClassName mapping from logical name to class name
     * @return the located class name or null
     */
    private String locate(String logicalName, Map<String, String> logicalNameToClassName)
    {
        rebuild();

        String result = logicalNameToClassName.get(logicalName);

        // If not found, see if it exists under the core package. In this way,
        // anything in core is "inherited" (but overridable) by the application.

        if (result == null) result = logicalNameToClassName.get(CORE_LIBRARY_PREFIX + logicalName);

        return result;
    }

    public String resolvePageClassNameToPageName(final String pageClassName)
    {
        return _barrier.withRead(new Invokable<String>()
        {
            public String invoke()
            {
                rebuild();

                String result = _pageClassNameToLogicalName.get(pageClassName);

                if (result == null) throw new IllegalArgumentException(ServicesMessages
                        .pageNameUnresolved(pageClassName));

                return result;
            }
        });
    }

    public String canonicalizePageName(final String pageName)
    {
        return _barrier.withRead(new Invokable<String>()
        {
            public String invoke()
            {
                String result = locate(pageName, _pageNameToCanonicalPageName);

                if (result == null) throw new IllegalArgumentException(ServicesMessages
                        .couldNotCanonicalizePageName(pageName, presentableNames(_pageNameToCanonicalPageName)));

                return result;
            }
        });
    }

}