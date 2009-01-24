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

package org.apache.tapestry5.internal.services;

import org.apache.tapestry5.internal.InternalConstants;
import org.apache.tapestry5.ioc.Invokable;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.ioc.annotations.Symbol;
import org.apache.tapestry5.ioc.internal.util.CollectionFactory;
import org.apache.tapestry5.ioc.internal.util.ConcurrentBarrier;
import org.apache.tapestry5.ioc.internal.util.InternalUtils;
import org.apache.tapestry5.ioc.services.ClassNameLocator;
import org.apache.tapestry5.services.ComponentClassResolver;
import org.apache.tapestry5.services.InvalidationListener;
import org.apache.tapestry5.services.LibraryMapping;
import org.slf4j.Logger;

import java.util.*;
import java.util.regex.Pattern;

public class ComponentClassResolverImpl implements ComponentClassResolver, InvalidationListener
{
    private static final String CORE_LIBRARY_PREFIX = "core/";

    private final Logger logger;

    private final ComponentInstantiatorSource componentInstantiatorSource;

    private final ClassNameLocator classNameLocator;

    private final String appRootPackage;

    // Map from folder name to a list of root package names.
    // The key does not begin or end with a slash.

    private final Map<String, List<String>> mappings = CollectionFactory.newCaseInsensitiveMap();

    // Flag indicating that the maps have been cleared following an invalidation
    // and need to be rebuilt. The flag and the four maps below are not synchronized
    // because they are only modified inside a synchronized block. That should be strong enough ...
    // and changes made will become "visible" at the end of the synchronized block. Because of the
    // structure of Tapestry, there should not be any reader threads while the write thread
    // is operating.

    private boolean needsRebuild = true;

    /**
     * Logical page name to class name.
     */
    private final Map<String, String> pageToClassName = CollectionFactory.newCaseInsensitiveMap();

    /**
     * Component type to class name.
     */
    private final Map<String, String> componentToClassName = CollectionFactory.newCaseInsensitiveMap();

    /**
     * Mixing type to class name.
     */
    private final Map<String, String> mixinToClassName = CollectionFactory.newCaseInsensitiveMap();

    /**
     * Page class name to logical name (needed to build URLs). This one is case sensitive, since class names do always
     * have a particular case.
     */
    private final Map<String, String> pageClassNameToLogicalName = CollectionFactory.newMap();


    /**
     * Used to convert a logical page name to the canonical form of the page name; this ensures that uniform case for
     * page names is used.
     */
    private final Map<String, String> pageNameToCanonicalPageName = CollectionFactory.newCaseInsensitiveMap();

    private final ConcurrentBarrier barrier = new ConcurrentBarrier();

    private static final Pattern SPLIT_PACKAGE_PATTERN = Pattern.compile("\\.");

    private static final Pattern SPLIT_FOLDER_PATTERN = Pattern.compile("/");

    private static final int LOGICAL_NAME_BUFFER_SIZE = 40;

    public ComponentClassResolverImpl(Logger logger,

                                      ComponentInstantiatorSource componentInstantiatorSource,

                                      ClassNameLocator classNameLocator,

                                      @Inject @Symbol(InternalConstants.TAPESTRY_APP_PACKAGE_PARAM)
                                      String appRootPackage,

                                      Collection<LibraryMapping> mappings)
    {
        this.logger = logger;
        this.componentInstantiatorSource = componentInstantiatorSource;
        this.classNameLocator = classNameLocator;

        this.appRootPackage = appRootPackage;

        addPackagesToInstantiatorSource(this.appRootPackage);

        for (LibraryMapping mapping : mappings)
        {
            String prefix = mapping.getPathPrefix();

            while (prefix.startsWith("/"))
            {
                prefix = prefix.substring(1);
            }

            while (prefix.endsWith("/"))
            {
                prefix = prefix.substring(0, prefix.length() - 1);
            }

            String rootPackage = mapping.getRootPackage();

            List<String> packages = this.mappings.get(prefix);

            if (packages == null)
            {
                packages = CollectionFactory.newList();
                this.mappings.put(prefix, packages);
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
        componentInstantiatorSource.addPackage(rootPackage + "." + InternalConstants.PAGES_SUBPACKAGE);
        componentInstantiatorSource.addPackage(rootPackage + "." + InternalConstants.COMPONENTS_SUBPACKAGE);
        componentInstantiatorSource.addPackage(rootPackage + "." + InternalConstants.MIXINS_SUBPACKAGE);
        componentInstantiatorSource.addPackage(rootPackage + "." + InternalConstants.BASE_SUBPACKAGE);
    }

    /**
     * When the class loader is invalidated, clear any cached page names or component types.
     */
    public synchronized void objectWasInvalidated()
    {
        barrier.withWrite(new Runnable()
        {
            public void run()
            {
                needsRebuild = true;
            }
        });
    }

    /**
     * Invoked from within a withRead() block, checks to see if a rebuild is needed, and then performs the rebuild
     * within a withWrite() block.
     */
    private void rebuild()
    {
        if (!needsRebuild) return;

        barrier.withWrite(new Runnable()
        {
            public void run()
            {
                performRebuild();
            }
        });
    }

    private void performRebuild()
    {

        Map<String, String> savedPages = CollectionFactory.newMap(pageToClassName);
        Map<String, String> savedComponents = CollectionFactory.newMap(componentToClassName);
        Map<String, String> savedMixins = CollectionFactory.newMap(mixinToClassName);

        pageToClassName.clear();
        componentToClassName.clear();
        mixinToClassName.clear();
        pageClassNameToLogicalName.clear();
        pageNameToCanonicalPageName.clear();

        rebuild("", appRootPackage);

        for (String prefix : mappings.keySet())
        {
            List<String> packages = mappings.get(prefix);

            String folder = prefix + "/";

            for (String packageName : packages)
                rebuild(folder, packageName);
        }


        showChanges("pages", savedPages, pageToClassName);
        showChanges("components", savedComponents, componentToClassName);
        showChanges("mixins", savedMixins, mixinToClassName);

        needsRebuild = false;
    }

    private void showChanges(String title, Map<String, String> savedMap, Map<String, String> newMap)
    {
        if (savedMap.equals(newMap)) return;

        Map<String, String> core = CollectionFactory.newMap();
        Map<String, String> nonCore = CollectionFactory.newMap();

        int maxLength = 0;

        // Pass # 1: Get all the stuff in the core library

        for (String name : newMap.keySet())
        {
            if (name.startsWith(CORE_LIBRARY_PREFIX))
            {
                // Strip off the "core/" prefix.

                String key = name.substring(CORE_LIBRARY_PREFIX.length());

                maxLength = Math.max(maxLength, key.length());

                core.put(key, newMap.get(name));
            }
            else
            {
                maxLength = Math.max(maxLength, name.length());

                nonCore.put(name, newMap.get(name));
            }
        }

        // Merge the non-core mappings into the core mappings. Where there are conflicts on name, it
        // means the application overrode a core page/component/mixin and that's ok ... the
        // merged core map will reflect the application's mapping.

        core.putAll(nonCore);

        StringBuilder builder = new StringBuilder(2000);
        Formatter f = new Formatter(builder);

        f.format("Available %s:\n", title);

        String formatString = "%" + maxLength + "s: %s\n";

        List<String> sorted = InternalUtils.sortedKeys(core);

        for (String name : sorted)
        {
            String className = core.get(name);

            if (name.equals("")) name = "(blank)";

            f.format(formatString, name, className);
        }

        logger.info(builder.toString());
    }

    private void rebuild(String pathPrefix, String rootPackage)
    {
        fillNameToClassNameMap(pathPrefix, rootPackage, InternalConstants.PAGES_SUBPACKAGE, pageToClassName);
        fillNameToClassNameMap(pathPrefix, rootPackage, InternalConstants.COMPONENTS_SUBPACKAGE, componentToClassName);
        fillNameToClassNameMap(pathPrefix, rootPackage, InternalConstants.MIXINS_SUBPACKAGE, mixinToClassName);
    }

    private void fillNameToClassNameMap(String pathPrefix, String rootPackage, String subPackage,
                                        Map<String, String> logicalNameToClassName)
    {
        String searchPackage = rootPackage + "." + subPackage;
        boolean isPage = subPackage.equals(InternalConstants.PAGES_SUBPACKAGE);

        Collection<String> classNames = classNameLocator.locateClassNames(searchPackage);

        int startPos = searchPackage.length() + 1;

        for (String name : classNames)
        {
            String logicalName = toLogicalName(name, pathPrefix, startPos, true);
            String unstrippedName = toLogicalName(name, pathPrefix, startPos, false);

            if (isPage)
            {
                int lastSlashx = logicalName.lastIndexOf("/");

                String lastTerm = lastSlashx < 0 ? logicalName : logicalName.substring(lastSlashx + 1);

                if (lastTerm.equalsIgnoreCase("index"))
                {
                    String reducedName = lastSlashx < 0 ? "" : logicalName.substring(0, lastSlashx);

                    // Make the super-stripped name another alias to the class.

                    logicalNameToClassName.put(reducedName, name);
                    pageNameToCanonicalPageName.put(reducedName, logicalName);
                }

                pageClassNameToLogicalName.put(name, logicalName);
                pageNameToCanonicalPageName.put(logicalName, logicalName);
                pageNameToCanonicalPageName.put(unstrippedName, logicalName);
            }

            logicalNameToClassName.put(logicalName, name);
            logicalNameToClassName.put(unstrippedName, name);
        }
    }


    /**
     * Converts a fully qualified class name to a logical name
     *
     * @param className  fully qualified class name
     * @param pathPrefix prefix to be placed on the logical name (to identify the library from in which the class
     *                   lives)
     * @param startPos   start position within the class name to extract the logical name (i.e., after the final '.' in
     *                   "rootpackage.pages.").
     * @param stripTerms
     * @return a short logical name in folder format ('.' replaced with '/')
     */
    private String toLogicalName(String className, String pathPrefix, int startPos, boolean stripTerms)
    {
        List<String> terms = CollectionFactory.newList();

        addAll(terms, SPLIT_FOLDER_PATTERN, pathPrefix);

        addAll(terms, SPLIT_PACKAGE_PATTERN, className.substring(startPos));

        StringBuilder builder = new StringBuilder(LOGICAL_NAME_BUFFER_SIZE);
        String sep = "";

        String logicalName = terms.remove(terms.size() - 1);

        String unstripped = logicalName;

        for (String term : terms)
        {
            builder.append(sep);
            builder.append(term);

            sep = "/";

            if (stripTerms) logicalName = stripTerm(term, logicalName);
        }

        if (logicalName.equals("")) logicalName = unstripped;

        builder.append(sep);
        builder.append(logicalName);

        return builder.toString();
    }

    private void addAll(List<String> terms, Pattern splitter, String input)
    {
        for (String term : splitter.split(input))
        {
            if (term.equals("")) continue;

            terms.add(term);
        }
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
        return barrier.withRead(new Invokable<String>()
        {
            public String invoke()
            {
                String result = locate(pageName, pageToClassName);

                if (result == null) throw new IllegalArgumentException(
                        ServicesMessages.couldNotResolvePageName(pageName, presentableNames(pageToClassName)));

                return result;
            }
        });
    }

    public boolean isPageName(final String pageName)
    {
        return barrier.withRead(new Invokable<Boolean>()
        {
            public Boolean invoke()
            {
                return locate(pageName, pageToClassName) != null;
            }
        });
    }

    public List<String> getPageNames()
    {
        return barrier.withRead(new Invokable<List<String>>()
        {
            public List<String> invoke()
            {
                rebuild();

                List<String> result = CollectionFactory.newList(pageClassNameToLogicalName.values());

                Collections.sort(result);

                return result;
            }
        });
    }

    public String resolveComponentTypeToClassName(final String componentType)
    {
        return barrier.withRead(new Invokable<String>()
        {
            public String invoke()
            {
                String result = locate(componentType, componentToClassName);

                if (result == null) throw new IllegalArgumentException(ServicesMessages
                        .couldNotResolveComponentType(componentType, presentableNames(componentToClassName)));

                return result;
            }
        });
    }

    Collection<String> presentableNames(Map<String, ?> map)
    {
        Set<String> result = CollectionFactory.newSet();

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
        return barrier.withRead(new Invokable<String>()
        {
            public String invoke()
            {
                String result = locate(mixinType, mixinToClassName);

                if (result == null) throw new IllegalArgumentException(
                        ServicesMessages.couldNotResolveMixinType(mixinType, presentableNames(mixinToClassName)));

                return result;
            }
        });
    }

    /**
     * Locates a class name within the provided map, given its logical name. If not found naturally, a search inside the
     * "core" library is included.
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
        return barrier.withRead(new Invokable<String>()
        {
            public String invoke()
            {
                rebuild();

                String result = pageClassNameToLogicalName.get(pageClassName);

                if (result == null) throw new IllegalArgumentException(ServicesMessages
                        .pageNameUnresolved(pageClassName));

                return result;
            }
        });
    }

    public String canonicalizePageName(final String pageName)
    {
        return barrier.withRead(new Invokable<String>()
        {
            public String invoke()
            {
                String result = locate(pageName, pageNameToCanonicalPageName);

                if (result == null) throw new IllegalArgumentException(ServicesMessages
                        .couldNotCanonicalizePageName(pageName, presentableNames(pageNameToCanonicalPageName)));

                return result;
            }
        });
    }
}
