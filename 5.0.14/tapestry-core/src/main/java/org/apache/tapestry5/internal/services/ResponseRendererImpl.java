// Copyright 2008 The Apache Software Foundation
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

import org.apache.tapestry5.ContentType;
import org.apache.tapestry5.internal.structure.Page;
import org.apache.tapestry5.ioc.internal.util.Defense;
import org.apache.tapestry5.runtime.Component;
import org.apache.tapestry5.services.ResponseRenderer;

import java.io.IOException;

public class ResponseRendererImpl implements ResponseRenderer
{
    private final RequestPageCache pageCache;

    private final PageContentTypeAnalyzer pageContentAnalyzer;

    private final PageResponseRenderer renderer;

    public ResponseRendererImpl(RequestPageCache pageCache, PageContentTypeAnalyzer pageContentAnalyzer,
                                PageResponseRenderer renderer)
    {
        this.pageCache = pageCache;
        this.pageContentAnalyzer = pageContentAnalyzer;
        this.renderer = renderer;
    }

    public ContentType findContentType(Object component)
    {
        Component c = Defense.cast(component, Component.class, "component");

        String pageName = c.getComponentResources().getPageName();

        Page page = pageCache.get(pageName);

        return pageContentAnalyzer.findContentType(page);
    }

    public void renderPageMarkupResponse(String pageName) throws IOException
    {
        Page page = pageCache.get(pageName);

        renderer.renderPageResponse(page);
    }
}
