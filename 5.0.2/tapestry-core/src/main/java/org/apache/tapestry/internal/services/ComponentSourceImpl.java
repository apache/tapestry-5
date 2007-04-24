// Copyright 2006 The Apache Software Foundation
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

import static org.apache.tapestry.ioc.internal.util.Defense.notNull;

import org.apache.tapestry.internal.structure.Page;
import org.apache.tapestry.runtime.Component;
import org.apache.tapestry.services.ComponentSource;

public class ComponentSourceImpl implements ComponentSource
{
    private final RequestPageCache _pageCache;

    public ComponentSourceImpl(RequestPageCache pageCache)
    {
        _pageCache = pageCache;
    }

    public Component getComponent(String componentId)
    {
        int colonx = componentId.indexOf(':');

        if (colonx < 0)
        {
            Page page = _pageCache.getByClassName(componentId);

            return page.getRootComponent();
        }

        String pageName = componentId.substring(0, colonx);

        Page page = _pageCache.getByClassName(pageName);
        String nestedId = componentId.substring(colonx + 1);

        return page.getComponentElementByNestedId(nestedId).getComponent();
    }

    public <T> T getPage(Class<T> pageClass)
    {
        notNull(pageClass, "pageClass");

        Page page = _pageCache.getByClassName(pageClass.getName());

        Component root = page.getRootComponent();

        return pageClass.cast(root);
    }

    public Component getPage(String pageName)
    {
        Page page = _pageCache.get(pageName);

        return page.getRootComponent();
    }
    
}
