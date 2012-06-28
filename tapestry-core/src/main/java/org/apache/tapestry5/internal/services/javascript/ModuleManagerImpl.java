// Copyright 2012 The Apache Software Foundation
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

package org.apache.tapestry5.internal.services.javascript;

import org.apache.tapestry5.dom.Element;
import org.apache.tapestry5.func.F;
import org.apache.tapestry5.func.Worker;
import org.apache.tapestry5.internal.services.assets.ResourceChangeTracker;
import org.apache.tapestry5.ioc.Resource;
import org.apache.tapestry5.ioc.annotations.PostInjection;
import org.apache.tapestry5.ioc.internal.util.CollectionFactory;
import org.apache.tapestry5.services.AssetSource;
import org.apache.tapestry5.services.ComponentClassResolver;
import org.apache.tapestry5.services.assets.AssetPathConstructor;
import org.apache.tapestry5.services.javascript.ModuleManager;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class ModuleManagerImpl implements ModuleManager
{
    private final String requireConfig;

    private final Map<String, Resource> configuration;

    // Library names, sorted by order of descending length.
    private final List<String> libraryNames;

    private final Map<String, List<String>> libraryNameToPackageNames = CollectionFactory.newMap();

    private final Resource classpathRoot;

    // Note: ConcurrentHashMap does not support null as a value, alas. We use classpathRoot as a null.
    private final Map<String, Resource> cache = CollectionFactory.newConcurrentMap();

    public ModuleManagerImpl(AssetPathConstructor constructor, final ComponentClassResolver resolver, AssetSource assetSource, Map<String, Resource> configuration)
    {
        this.configuration = configuration;
        String baseURL = constructor.constructAssetPath("module-root", "");

        requireConfig = String.format("require.config({baseUrl:\"%s\"});\n",
                baseURL);

        classpathRoot = assetSource.resourceForPath("");

        libraryNames = F.flow(resolver.getLibraryNames())
                .each(new Worker<String>()
                {
                    @Override
                    public void work(String element)
                    {
                        libraryNameToPackageNames.put(element, resolver.getPackagesForLibrary(element));
                    }
                })
                .append("app")
                .sort(new Comparator<String>()
                {
                    @Override
                    public int compare(String o1, String o2)
                    {
                        return o2.length() - o1.length();
                    }
                }).toList();

        libraryNameToPackageNames.put("app", resolver.getPackagesForLibrary(""));
    }

    @PostInjection
    public void setupInvalidation(ResourceChangeTracker tracker)
    {
        tracker.clearOnInvalidation(cache);
    }

    @Override
    public void writeConfiguration(Element scriptElement)
    {
        scriptElement.raw(requireConfig);
    }

    @Override
    public Resource findResourceForModule(String moduleName)
    {
        Resource resource = cache.get(moduleName);

        if (resource == null)
        {
            resource = resolveModuleNameToResource(moduleName);
            cache.put(moduleName, resource);
        }


        // We're treating classpathRoot as a placeholder for null.

        return resource == classpathRoot ? null : resource;
    }

    private Resource resolveModuleNameToResource(String moduleName)
    {
        Resource resource = configuration.get(moduleName);

        if (resource != null)
        {
            if (!resource.exists())
            {
                throw new RuntimeException(String.format("Resource %s (mapped as module '%s') does not exist.",
                        resource, moduleName));
            }

            return resource;
        }

        // Look for the longest match.

        for (String library : libraryNames)
        {
            int len = library.length();

            if (moduleName.length() <= len)
            {
                continue;
            }

            if (moduleName.startsWith(library) && moduleName.charAt(len) == '/')
            {
                return findResourceInsideLibrary(library, moduleName);
            }
        }

        return classpathRoot;
    }

    private Resource findResourceInsideLibrary(String library, String moduleName)
    {
        String extra = moduleName.substring(library.length() + 1);

        List<String> packages = libraryNameToPackageNames.get(library);

        for (String p : packages)
        {
            String baseName = p.replace('.', '/') + "/modulejs/" + extra;

            Resource baseResource = classpathRoot.forFile(baseName);

            // TODO: Figure out which suffixes to try for. More configuration, somewhere, to indicate
            // that .coffee will be converted to .js. Maybe add a method to ResourceTransformer; the MIME type
            // produced.

            Resource resource = baseResource.withExtension("js");

            if (resource.exists())
            {
                return resource;
            }
        }

        // Not found in any package

        return classpathRoot;
    }
}
