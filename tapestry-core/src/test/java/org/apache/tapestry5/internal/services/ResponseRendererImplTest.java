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

import org.apache.tapestry5.ComponentResources;
import org.apache.tapestry5.ContentType;
import org.apache.tapestry5.internal.structure.Page;
import org.apache.tapestry5.internal.test.InternalBaseTestCase;
import org.apache.tapestry5.runtime.Component;
import org.apache.tapestry5.services.Response;
import org.apache.tapestry5.services.ResponseRenderer;
import org.testng.annotations.Test;

public class ResponseRendererImplTest extends InternalBaseTestCase
{
    @Test
    public void content_type_from_component()
    {
        RequestPageCache cache = mockRequestPageCache();
        PageContentTypeAnalyzer analyzer = mockPageContentTypeAnalyzer();
        Component component = mockComponent();
        String pageName = "foo/bar";
        Page page = mockPage();
        ContentType contentType = new ContentType("zig/zag");
        ComponentResources resources = mockComponentResources();

        train_getComponentResources(component, resources);
        train_getPageName(resources, pageName);
        train_get(cache, pageName, page);

        train_findContentType(analyzer, page, contentType);

        replay();

        ResponseRenderer renderer = new ResponseRendererImpl(cache, analyzer, null);

        assertSame(renderer.findContentType(component), contentType);

        verify();
    }

    @Test
    public void render_page_markup() throws Exception
    {
        RequestPageCache cache = mockRequestPageCache();
        PageContentTypeAnalyzer analyzer = mockPageContentTypeAnalyzer();
        String pageName = "foo/bar";
        Page page = mockPage();
        PageResponseRenderer pageResponseRenderer = mockPageResponseRenderer();
        Response response = mockResponse();

        train_get(cache, pageName, page);

        pageResponseRenderer.renderPageResponse(page);

        replay();

        ResponseRenderer renderer = new ResponseRendererImpl(cache, analyzer, pageResponseRenderer);

        renderer.renderPageMarkupResponse(pageName);

        verify();
    }

}
