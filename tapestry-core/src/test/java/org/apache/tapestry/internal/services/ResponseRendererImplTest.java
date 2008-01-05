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

import org.apache.tapestry.ComponentResources;
import org.apache.tapestry.ContentType;
import org.apache.tapestry.internal.structure.Page;
import org.apache.tapestry.internal.test.InternalBaseTestCase;
import org.apache.tapestry.runtime.Component;
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

        ResponseRenderer renderer = new ResponseRendererImpl(cache, analyzer);

        assertSame(renderer.findContentType(component), contentType);

        verify();
    }

}
