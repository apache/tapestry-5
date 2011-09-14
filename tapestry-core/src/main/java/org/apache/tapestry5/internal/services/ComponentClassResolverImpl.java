// Copyright 2006, 2007, 2008, 2009, 2010, 2011 The Apache Software Foundation
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

package org.apache.tapestry5.internal.services;

import org.apache.tapestry5.SymbolConstants;
import org.apache.tapestry5.internal.InternalConstants;
import org.apache.tapestry5.ioc.annotations.Symbol;
import org.apache.tapestry5.ioc.internal.util.CollectionFactory;
import org.apache.tapestry5.ioc.internal.util.InternalUtils;
import org.apache.tapestry5.ioc.services.ClassNameLocator;
import org.apache.tapestry5.ioc.util.AvailableValues;
import org.apache.tapestry5.ioc.util.UnknownValueException;
import org.apache.tapestry5.services.ComponentClassResolver;
import org.apache.tapestry5.services.InvalidationListener;
import org.apache.tapestry5.services.LibraryMapping;
import org.apache.tapestry5.services.transform.ControlledPackageType;
import org.slf4j.Logger;

import java.util.*;
import java.util.regex.Pattern;

public class ComponentClassResolverImpl implements ComponentClassResolver, InvalidationListener
{
    private static final String CORE_LIBRARY_PREFIX = "core/";

    private static final Pattern SPLIT_PACKAGE_PATTERN = Pattern.compile("\\.");

    private static final Pattern SPLIT_FOLDER_PATTERN = Pattern.compile("/");

    private static final int LOGICAL_NAME_BUFFER_SIZE = 40;

    private final Logger logger;

    private final ClassNameLocator classNameLocator;

    private final String startPageName;

    // Map from folder name to a list of root package names.
    // The key does not begin or end with a slash.

    private final Map<String, List<String>> mappings = CollectionFactory.newCaseInsensitiveMap();

    private final Map<String, ControlledPackageType> packageMappings = CollectionFactory.newMap();

    // Flag indicating that the maps have been cleared following an invalidation
    // and need to be rebuilt. The flag and the four maps below are not synchronized
    // because they are only modified inside a synchronized block. That should be strong enough ...
    // and changes made will become "visible" at the end of the synchronized block. Because of the
    // structure of Tapestry, there should not be any reader threads while the write thread
    // is operating.

    private volatile boolean needsRebuild = true;

    private class Data
    {

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

                    if (lastTerm.equalsIgnoreCase("index") || lastTerm.equalsIgnoreCase(startPageName))
                    {
                        String reducedName = lastSlashx < 0 ? "" : logicalName.substring(0, lastSlashx);

                        // Make the super-stripped name another alias to the class.
                        // TAP5-1444: Everything else but a start page has precedence

                        if (!(lastTerm.equalsIgnoreCase(startPageName) && logicalNameToClassName.containsKey(reducedName)))
                        {
                            logicalNameToClassName.put(reducedName, name);
                            pageNameToCanonicalPageName.put(reducedName, logicalName);
                        }
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

                if (stripTerms)
                    logicalName = stripTerm(term, logicalName);
            }

            if (logicalName.equals(""))
                logicalName = unstripped;

            builder.append(sep);
            builder.append(logicalName);

            return builder.toString();
        }

        private void addAll(List<String> terms, Pattern splitter, String input)
        {
            for (String term : splitter.split(input))
            {
                if (term.equals(""))
                    continue;

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
            return string.regionMatches(true, string.length() - suffix.length(), suffix, 0, suffix.length());
        }
    }

    private volatile Data data = new Data();

    public ComponentClassResolverImpl(Logger logger,

                                      ClassNameLocator classNameLocator,

                                      @Symbol(SymbolConstants.START_PAGE_NAME)
                                      String startPageName,

                                      Collection<LibraryMapping> mappings)
    {
        this.logger = logger;
        this.classNameLocator = classNameLocator;

        this.startPageName = startPageName;

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

            addSubpackagesToPackageMapping(rootPackage);
        }
    }

    private void addSubpackagesToPackageMapping(String rootPackage)
    {
        for (String subpackage : InternalConstants.SUBPACKAGES)
        {
            packageMappings.put(rootPackage + "." + subpackage, ControlledPackageType.COMPONENT);
        }
    }

    public Map<String, ControlledPackageType> getControlledPackageMapping()
    {
        return Collections.unmodifiableMap(packageMappings);
    }

    /**
     * When the class loader is invalidated, clear any cached page names or component types.
     */
    public synchronized void objectWasInvalidated()
    {
        needsRebuild = true;
    }

    /**
     * Invoked from within a withRead() block, checks to see if a rebuild is needed, and then performs the rebuild
     * within a withWrite() block.
     */
    private Data getData()
    {
        if (!needsRebuild)
        {
            return data;
        }

        Data newData = new Data();

        for (String prefix : mappings.keySet())
        {
            List<String> packages = mappings.get(prefix);

            String folder = prefix + "/";

            for (String packageName : packages)
                newData.rebuild(folder, packageName);
        }

        showChanges("pages", data.pageToClassName, newData.pageToClassName);
        showChanges("components", data.componentToClassName, newData.componentToClassName);
        showChanges("mixins", data.mixinToClassName, newData.mixinToClassName);

        needsRebuild = false;

        data = newData;

        return data;
    }

    private static int countUnique(Map<String, String> map)
    {
        return CollectionFactory.newSet(map.values()).size();
    }

    private void showChanges(String title, Map<String, String> savedMap, Map<String, String> newMap)
    {
        if (savedMap.equals(newMap))
            return;

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
            } else
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

        int oldCount = countUnique(savedMap);
        int newCount = countUnique(newMap);

        f.format("Available %s (%d", title, newCount);

        if (oldCount > 0 && oldCount != newCount)
        {
            f.format(", +%d", newCount - oldCount);
        }

        builder.append("):\n");

        String formatString = "%" + maxLength + "s: %s\n";

        List<String> sorted = InternalUtils.sortedKeys(core);

        for (String name : sorted)
        {
            String className = core.get(name);

            if (name.equals(""))
                name = "(blank)";

            f.format(formatString, name, className);
        }

        logger.info(builder.toString());
    }


    public String resolvePageNameToClassName(final String pageName)
    {
        Data data = getData();

        String result = locate(pageName, data.pageToClassName);

        if (result == null)
        {
            throw new UnknownValueException(String.format("Unable to resolve '%s' to a page class name.",
                    pageName), new AvailableValues("Page names", presentableNames(data.pageToClassName)));
        }

        return result;
    }

    public boolean isPageName(final String pageName)
    {
        return locate(pageName, getData().pageToClassName) != null;
    }

    public boolean isPage(final String pageClassName)
    {
        return locate(pageClassName, getData().pageClassNameToLogicalName) != null;
    }

    public List<String> getPageNames()
    {
        Data data = getData();

        List<String> result = CollectionFactory.newList(data.pageClassNameToLogicalName.values());

        Collections.sort(result);

        return result;
    }

    public String resolveComponentTypeToClassName(final String componentType)
    {
        Data data = getData();

        String result = locate(componentType, data.componentToClassName);

        if (result == null)
        {
            throw new UnknownValueException(String.format("Unable to resolve '%s' to a component class name.",
                    componentType), new AvailableValues("Component types",
                    presentableNames(data.componentToClassName)));
        }

        return result;
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
        Data data = getData();

        String result = locate(mixinType, data.mixinToClassName);

        if (result == null)
        {
            throw new UnknownValueException(String.format("Unable to resolve '%s' to a mixin class name.",
                    mixinType), new AvailableValues("Mixin types", presentableNames(data.mixinToClassName)));
        }

        return result;
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
        String result = logicalNameToClassName.get(logicalName);

        // If not found, see if it exists under the core package. In this way,
        // anything in core is "inherited" (but overridable) by the application.

        if (result != null)
        {
            return result;
        }

        return logicalNameToClassName.get(CORE_LIBRARY_PREFIX + logicalName);
    }

    public String resolvePageClassNameToPageName(final String pageClassName)
    {
        String result = getData().pageClassNameToLogicalName.get(pageClassName);

        if (result == null)
        {
            throw new IllegalArgumentException(ServicesMessages.pageNameUnresolved(pageClassName));
        }

        return result;
    }

    public String canonicalizePageName(final String pageName)
    {
        Data data = getData();

        String result = locate(pageName, data.pageNameToCanonicalPageName);

        if (result == null)
        {
            throw new UnknownValueException(String.format("Unable to resolve '%s' to a known page name.",
                    pageName), new AvailableValues("Page names", presentableNames(data.pageNameToCanonicalPageName)));
        }

        return result;
    }

    public Map<String, String> getFolderToPackageMapping()
    {
        Map<String, String> result = CollectionFactory.newCaseInsensitiveMap();

        for (String folder : mappings.keySet())
        {
            List<String> packageNames = mappings.get(folder);

            String packageName = findCommonPackageNameForFolder(folder, packageNames);

            result.put(folder, packageName);
        }

        return result;
    }

    static String findCommonPackageNameForFolder(String folder, List<String> packageNames)
    {
        String packageName = findCommonPackageName(packageNames);

        if (packageName == null)
            throw new RuntimeException(
                    String.format(
                            "Package names for library folder '%s' (%s) can not be reduced to a common base package (of at least one term).",
                            folder, InternalUtils.joinSorted(packageNames)));
        return packageName;
    }

    static String findCommonPackageName(List<String> packageNames)
    {
        // BTW, this is what reduce is for in Clojure ...

        String commonPackageName = packageNames.get(0);

        for (int i = 1; i < packageNames.size(); i++)
        {
            commonPackageName = findCommonPackageName(commonPackageName, packageNames.get(i));

            if (commonPackageName == null)
                break;
        }

        return commonPackageName;
    }

    static String findCommonPackageName(String commonPackageName, String packageName)
    {
        String[] commonExploded = explode(commonPackageName);
        String[] exploded = explode(packageName);

        int count = Math.min(commonExploded.length, exploded.length);

        int commonLength = 0;
        int commonTerms = 0;

        for (int i = 0; i < count; i++)
        {
            if (exploded[i].equals(commonExploded[i]))
            {
                // Keep track of the number of shared characters (including the dot seperators)

                commonLength += exploded[i].length() + (i == 0 ? 0 : 1);
                commonTerms++;
            } else
            {
                break;
            }
        }

        if (commonTerms < 1)
            return null;

        return commonPackageName.substring(0, commonLength);
    }

    private static final Pattern DOT = Pattern.compile("\\.");

    private static String[] explode(String packageName)
    {
        return DOT.split(packageName);
    }
}
