// Copyright 2006-2014 The Apache Software Foundation
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

import org.apache.tapestry5.commons.Resource;
import org.apache.tapestry5.commons.util.AvailableValues;
import org.apache.tapestry5.commons.util.CollectionFactory;
import org.apache.tapestry5.commons.util.UnknownValueException;
import org.apache.tapestry5.services.AssetAlias;
import org.apache.tapestry5.services.ClasspathAssetAliasManager;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class ClasspathAssetAliasManagerImpl implements ClasspathAssetAliasManager
{
    /**
     * Map from alias to path.
     */
    private final Map<String, String> aliasToPathPrefix = CollectionFactory.newMap();

    /**
     * Map from path to alias.
     */
    private final Map<String, String> pathPrefixToAlias = CollectionFactory.newMap();

    private final List<String> sortedPathPrefixes;

    /**
     * Configuration is a map of aliases (short names) to complete names. Keys and values must not start or end with a slash,
     * but may contain them.
     */
    public ClasspathAssetAliasManagerImpl(Map<String, String> configuration)
    {
        for (Map.Entry<String, String> e : configuration.entrySet())
        {
            String alias = verify("folder name", e.getKey());

            String path = verify("path", e.getValue());

            aliasToPathPrefix.put(alias, path);
            pathPrefixToAlias.put(path, alias);
        }

        Comparator<String> sortDescendingByLength = new Comparator<String>()
        {
            public int compare(String o1, String o2)
            {
                return o2.length() - o1.length();
            }
        };

        sortedPathPrefixes = CollectionFactory.newList(aliasToPathPrefix.values());
        Collections.sort(sortedPathPrefixes, sortDescendingByLength);
    }

    private String verify(String name, String input)
    {

        if (input.startsWith("/") || input.endsWith("/"))
            throw new RuntimeException(String.format("Contribution of %s '%s' is invalid as it may not start with or end with a slash.",
                    name, input));

        return input;

    }

    public AssetAlias extractAssetAlias(Resource resource)
    {
        String resourcePath = resource.getPath();

        for (String pathPrefix : sortedPathPrefixes)
        {
            if (resourcePath.startsWith(pathPrefix))
            {
                if (pathPrefix.length() == resourcePath.length())
                {
                    throw new IllegalArgumentException(String.format("Resource path '%s' is not valid as it is mapped as virtual folder '%s'.",
                            resourcePath,
                            pathPrefixToAlias.get(pathPrefix)));
                }

                // Prevent matching path prefix "foo" against "foobar" ... it must match against "foo/".
                if (resourcePath.charAt(pathPrefix.length()) != '/')
                {
                    continue;
                }

                String virtualFolder = pathPrefixToAlias.get(pathPrefix);

                // Don't want that slash seperating the folder from the rest of the path.
                String path = resourcePath.substring(pathPrefix.length() + 1);

                return new AssetAlias(virtualFolder, path);
            }
        }

        // This is a minor misuse of the UnknownValueException but the exception reporting
        // is too useful to pass up.

        throw new UnknownValueException(
                String.format(
                        "Unable to create a client URL for classpath resource %s: The resource path was not within an aliased path.",
                        resourcePath), new AvailableValues("Aliased paths", aliasToPathPrefix.values()));
    }

    public Map<String, String> getMappings()
    {
        return Collections.unmodifiableMap(aliasToPathPrefix);
    }

}
