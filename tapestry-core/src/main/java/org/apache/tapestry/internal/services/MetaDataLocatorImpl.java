// Copyright 2007 The Apache Software Foundation
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

import org.apache.tapestry.ComponentResources;
import org.apache.tapestry.internal.events.InvalidationListener;
import org.apache.tapestry.ioc.internal.util.CollectionFactory;
import static org.apache.tapestry.ioc.internal.util.CollectionFactory.newCaseInsensitiveMap;
import static org.apache.tapestry.ioc.internal.util.CollectionFactory.newConcurrentMap;
import org.apache.tapestry.services.MetaDataLocator;

import java.util.Map;

public class MetaDataLocatorImpl implements MetaDataLocator, InvalidationListener
{
    private final Map<String, Map<String, String>> _defaultsByFolder = newCaseInsensitiveMap();

    private final Map<String, String> _cache = newConcurrentMap();

    public MetaDataLocatorImpl(Map<String, String> configuration)
    {
        loadDefaults(configuration);
    }

    public void objectWasInvalidated()
    {
        _cache.clear();
    }

    private void loadDefaults(Map<String, String> configuration)
    {
        for (Map.Entry<String, String> e : configuration.entrySet())
        {
            String key = e.getKey();

            int colonx = key.indexOf(':');

            String folderKey = colonx < 0 ? "" : key.substring(0, colonx);

            Map<String, String> forFolder = _defaultsByFolder.get(folderKey);

            if (forFolder == null)
            {
                forFolder = CollectionFactory.newCaseInsensitiveMap();
                _defaultsByFolder.put(folderKey, forFolder);
            }

            String defaultKey = colonx < 0 ? key : key.substring(colonx + 1);

            forFolder.put(defaultKey, e.getValue());
        }
    }

    public String findMeta(String key, ComponentResources resources)
    {
        // The component's complete id should be sufficient as locale-specific
        // values don't enter into this.

        String cacheKey = resources.getCompleteId() + "/" + key;

        if (_cache.containsKey(cacheKey)) return _cache.get(cacheKey);

        String result = locate(key, resources);

        _cache.put(cacheKey, result);

        return result;
    }

    private String locate(String key, ComponentResources resources)
    {
        ComponentResources cursor = resources;

        while (true)
        {
            String value = cursor.getComponentModel().getMeta(key);

            if (value != null) return value;

            ComponentResources next = cursor.getContainerResources();

            if (next == null) return locateInDefaults(key, cursor);

            cursor = next;
        }
    }

    private String locateInDefaults(String key, ComponentResources pageResources)
    {
        String logicalName = pageResources.getPageName();

        // We're going to peel this apart, slash by slash. Thus for
        // "mylib/myfolder/mysubfolder/MyPage" we'll be checking: "mylib/myfolder/mysubfolder",
        // then "mylib/myfolder", then "mylib", then "".

        String path = logicalName;

        while (true)
        {
            int lastSlashx = path.lastIndexOf('/');

            String folderKey = lastSlashx < 0 ? "" : path.substring(0, lastSlashx);

            Map<String, String> forFolder = _defaultsByFolder.get(folderKey);

            if (forFolder != null && forFolder.containsKey(key)) return forFolder.get(key);

            if (lastSlashx < 0) break;

            path = path.substring(0, lastSlashx);
        }

        // Perhaps from here into the symbol sources? That may come later.

        return null;
    }

}
