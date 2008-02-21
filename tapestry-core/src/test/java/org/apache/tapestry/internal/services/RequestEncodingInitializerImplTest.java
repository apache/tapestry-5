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

package org.apache.tapestry.internal.services;

import org.apache.tapestry.TapestryConstants;
import org.apache.tapestry.internal.InternalComponentResources;
import org.apache.tapestry.internal.structure.ComponentPageElement;
import org.apache.tapestry.internal.structure.Page;
import org.apache.tapestry.internal.test.InternalBaseTestCase;
import org.apache.tapestry.services.MetaDataLocator;
import org.apache.tapestry.services.Request;
import org.testng.annotations.Test;

public class RequestEncodingInitializerImplTest extends InternalBaseTestCase
{
    @Test
    public void encoding_in_content_type()
    {
        RequestPageCache cache = mockRequestPageCache();
        Page page = mockPage();
        ComponentPageElement element = mockComponentPageElement();
        InternalComponentResources resources = mockInternalComponentResources();
        MetaDataLocator locator = mockMetaDataLocator();
        Request request = mockRequest();
        String pageName = "MyPage";

        train_get(cache, pageName, page);
        train_getRootElement(page, element);
        train_getComponentResources(element, resources);

        train_findMeta(
                locator,
                TapestryConstants.RESPONSE_CONTENT_TYPE,
                resources,
                String.class,
                "text/html;charset=zebra");

        request.setEncoding("zebra");

        replay();

        RequestEncodingInitializer init = new RequestEncodingInitializerImpl(cache, locator,
                                                                             request);

        init.initializeRequestEncoding(pageName);

        verify();
    }

    @Test
    public void encoding_on_second_meta_data()
    {
        RequestPageCache cache = mockRequestPageCache();
        Page page = mockPage();
        ComponentPageElement element = mockComponentPageElement();
        InternalComponentResources resources = mockInternalComponentResources();
        MetaDataLocator locator = mockMetaDataLocator();
        Request request = mockRequest();
        String pageName = "MyPage";
        String encoding = "ostritch";

        train_get(cache, pageName, page);
        train_getRootElement(page, element);
        train_getComponentResources(element, resources);

        train_findMeta(locator, TapestryConstants.RESPONSE_CONTENT_TYPE, resources, String.class, "text/html");

        train_findMeta(locator, TapestryConstants.RESPONSE_ENCODING, resources, String.class, encoding);

        request.setEncoding(encoding);

        replay();

        RequestEncodingInitializer init = new RequestEncodingInitializerImpl(cache, locator,
                                                                             request);

        init.initializeRequestEncoding(pageName);

        verify();
    }


}
