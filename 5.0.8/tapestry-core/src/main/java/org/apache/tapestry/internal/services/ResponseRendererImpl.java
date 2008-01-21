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

package org.apache.tapestry.internal.services;

import org.apache.tapestry.ContentType;
import org.apache.tapestry.internal.structure.Page;
import org.apache.tapestry.ioc.internal.util.Defense;
import org.apache.tapestry.runtime.Component;
import org.apache.tapestry.services.Response;

import java.io.IOException;

public class ResponseRendererImpl implements ResponseRenderer
{
    private final RequestPageCache _pageCache;

    private final PageContentTypeAnalyzer _pageContentAnalyzer;

    private final PageResponseRenderer _renderer;

    private final Response _response;

    public ResponseRendererImpl(RequestPageCache pageCache, PageContentTypeAnalyzer pageContentAnalyzer,
                                PageResponseRenderer renderer, Response response)
    {
        _pageCache = pageCache;
        _pageContentAnalyzer = pageContentAnalyzer;
        _renderer = renderer;
        _response = response;
    }

    public ContentType findContentType(Object component)
    {
        Component c = Defense.cast(component, Component.class, "component");

        String pageName = c.getComponentResources().getPageName();

        Page page = _pageCache.get(pageName);

        return _pageContentAnalyzer.findContentType(page);
    }

    public void renderPageMarkupResponse(String pageName) throws IOException
    {
        Page page = _pageCache.get(pageName);

        _renderer.renderPageResponse(page);
    }
}
