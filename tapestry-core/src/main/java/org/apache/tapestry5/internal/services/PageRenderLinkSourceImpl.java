// Copyright 2009 The Apache Software Foundation
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

import org.apache.tapestry5.services.PageRenderLinkSource;
import org.apache.tapestry5.services.ComponentClassResolver;
import org.apache.tapestry5.Link;

public class PageRenderLinkSourceImpl implements PageRenderLinkSource
{
    private final ComponentClassResolver resolver;

    private final LinkSource linkSource;

    public PageRenderLinkSourceImpl(LinkSource linkSource, ComponentClassResolver resolver)
    {
        this.linkSource = linkSource;
        this.resolver = resolver;
    }

    private String toPageName(Class pageClass)
    {
        return resolver.resolvePageClassNameToPageName(pageClass.getName());
    }

    public Link createPageRenderLink(Class pageClass)
    {
        return createPageRenderLink(toPageName(pageClass));
    }

    public Link createPageRenderLink(String pageName)
    {
        return linkSource.createPageRenderLink(pageName, false);
    }

    public Link createPageRenderLinkWithContext(Class pageClass, Object... context)
    {
        return createPageRenderLinkWithContext(toPageName(pageClass), context);
    }

    public Link createPageRenderLinkWithContext(String pageName, Object... context)
    {
        return linkSource.createPageRenderLink(pageName, true, context);
    }
}
