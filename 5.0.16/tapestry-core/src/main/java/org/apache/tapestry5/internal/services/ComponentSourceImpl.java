// Copyright 2006, 2007, 2008 The Apache Software Foundation
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
import org.apache.tapestry5.internal.structure.ComponentPageElement;
import org.apache.tapestry5.internal.structure.Page;
import org.apache.tapestry5.ioc.internal.util.Defense;
import org.apache.tapestry5.runtime.Component;
import org.apache.tapestry5.services.ComponentClassResolver;
import org.apache.tapestry5.services.ComponentSource;

public class ComponentSourceImpl implements ComponentSource
{
    private final RequestPageCache pageCache;

    private final ComponentClassResolver resolver;

    public ComponentSourceImpl(RequestPageCache pageCache, ComponentClassResolver resolver)
    {
        this.pageCache = pageCache;
        this.resolver = resolver;
    }

    public Component getComponent(String completeId)
    {
        Defense.notBlank(completeId, "completeId");

        int colonx = completeId.indexOf(':');

        if (colonx < 0)
        {
            Page page = pageCache.get(completeId);

            return page.getRootComponent();
        }

        String pageName = completeId.substring(0, colonx);

        Page page = pageCache.get(pageName);
        String nestedId = completeId.substring(colonx + 1);
        String mixinId = null;

        int dollarx = nestedId.indexOf("$");

        if (dollarx > 0)
        {
            mixinId = nestedId.substring(dollarx + 1);
            nestedId = nestedId.substring(0, dollarx);
        }


        ComponentPageElement element = page.getComponentElementByNestedId(nestedId);

        if (mixinId == null)
            return element.getComponent();

        ComponentResources resources = element.getMixinResources(mixinId);

        return resources.getComponent();
    }

    public Component getPage(String pageName)
    {
        Defense.notNull(pageName, "pageName");

        Page page = pageCache.get(pageName);

        return page.getRootComponent();
    }

    public Component getPage(Class pageClass)
    {
        Defense.notNull(pageClass, "pageClass");

        String pageName = resolver.resolvePageClassNameToPageName(pageClass.getName());

        return getPage(pageName);
    }
}
