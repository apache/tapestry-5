// Copyright 2007, 2008 The Apache Software Foundation
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

import org.apache.tapestry5.ComponentResources;
import org.apache.tapestry5.ioc.internal.util.CollectionFactory;
import org.apache.tapestry5.ioc.services.SymbolSource;
import org.apache.tapestry5.ioc.services.TypeCoercer;
import org.apache.tapestry5.services.InvalidationListener;
import org.apache.tapestry5.services.MetaDataLocator;

import java.util.Map;

public class MetaDataLocatorImpl implements MetaDataLocator, InvalidationListener
{
    private final SymbolSource symbolSource;

    private final TypeCoercer typeCoercer;

    private final ComponentModelSource modelSource;

    private final Map<String, Map<String, String>> defaultsByFolder = CollectionFactory.newCaseInsensitiveMap();

    private final Map<String, String> cache = CollectionFactory.newConcurrentMap();

    private interface ValueLocator
    {
        String valueForKey(String key);
    }

    public MetaDataLocatorImpl(SymbolSource symbolSource, TypeCoercer typeCoercer, ComponentModelSource modelSource,
                               Map<String, String> configuration
    )
    {
        this.symbolSource = symbolSource;
        this.typeCoercer = typeCoercer;
        this.modelSource = modelSource;

        loadDefaults(configuration);
    }

    public void objectWasInvalidated()
    {
        cache.clear();
    }

    private void loadDefaults(Map<String, String> configuration)
    {
        for (Map.Entry<String, String> e : configuration.entrySet())
        {
            String key = e.getKey();

            int colonx = key.indexOf(':');

            String folderKey = colonx < 0 ? "" : key.substring(0, colonx);

            Map<String, String> forFolder = defaultsByFolder.get(folderKey);

            if (forFolder == null)
            {
                forFolder = CollectionFactory.newCaseInsensitiveMap();
                defaultsByFolder.put(folderKey, forFolder);
            }

            String defaultKey = colonx < 0 ? key : key.substring(colonx + 1);

            forFolder.put(defaultKey, e.getValue());
        }
    }

    public <T> T findMeta(String key, final ComponentResources resources, Class<T> expectedType)
    {
        String value = getSymbolExpandedValueFromCache(key,
                                                       resources.getCompleteId() + "/" + key,
                                                       new ValueLocator()
                                                       {
                                                           public String valueForKey(String key)
                                                           {
                                                               return locate(key, resources);
                                                           }
                                                       });

        return typeCoercer.coerce(value, expectedType);
    }

    public <T> T findMeta(String key, final String pageName, Class<T> expectedType)
    {

        String value = getSymbolExpandedValueFromCache(key,
                                                       pageName + "/" + key,
                                                       new ValueLocator()
                                                       {
                                                           public String valueForKey(String key)
                                                           {
                                                               return modelSource.getPageModel(pageName).getMeta(key);
                                                           }
                                                       });

        return typeCoercer.coerce(value, expectedType);
    }

    private String getSymbolExpandedValueFromCache(String key, String cacheKey, ValueLocator valueLocator)
    {
        if (cache.containsKey(cacheKey))
            return cache.get(cacheKey);

        String value = valueLocator.valueForKey(key);

        if (value == null)
        {
            value = symbolSource.valueForSymbol(key);
        }
        else
        {
            value = symbolSource.expandSymbols(value);
        }

        cache.put(cacheKey, value);

        return value;
    }

    private String locate(String key, ComponentResources resources)
    {
        ComponentResources cursor = resources;

        while (true)
        {
            String value = cursor.getComponentModel().getMeta(key);

            if (value != null) return value;

            ComponentResources next = cursor.getContainerResources();

            if (next == null) return locateInDefaults(key, cursor.getPageName());

            cursor = next;
        }
    }

    private String locateInDefaults(String key, String pageName)
    {

        // We're going to peel this apart, slash by slash. Thus for
        // "mylib/myfolder/mysubfolder/MyPage" we'll be checking: "mylib/myfolder/mysubfolder",
        // then "mylib/myfolder", then "mylib", then "".

        String path = pageName;

        while (true)
        {
            int lastSlashx = path.lastIndexOf('/');

            String folderKey = lastSlashx < 0 ? "" : path.substring(0, lastSlashx);

            Map<String, String> forFolder = defaultsByFolder.get(folderKey);

            if (forFolder != null && forFolder.containsKey(key)) return forFolder.get(key);

            if (lastSlashx < 0) break;

            path = path.substring(0, lastSlashx);
        }

        // Perhaps from here into the symbol sources? That may come later.

        return null;
    }
}
