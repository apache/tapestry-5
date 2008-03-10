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
import org.apache.tapestry.Link;
import org.apache.tapestry.TapestryConstants;
import org.apache.tapestry.internal.structure.Page;
import org.apache.tapestry.internal.test.InternalBaseTestCase;
import org.apache.tapestry.runtime.Component;
import org.apache.tapestry.services.BaseURLSource;
import org.apache.tapestry.services.MetaDataLocator;
import org.apache.tapestry.services.Request;
import org.apache.tapestry.services.Response;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class RequestSecurityManagerImplTest extends InternalBaseTestCase
{
    private static final String PAGE_NAME = "Whatever";

    @Test
    public void check_request_is_secure() throws Exception
    {
        Request request = mockRequest();
        Response response = mockResponse();
        LinkFactory linkFactory = mockLinkFactory();
        MetaDataLocator locator = mockMetaDataLocator();
        BaseURLSource source = mockBaseURLSource();
        RequestPageCache cache = mockRequestPageCache();

        train_isSecure(request, true);

        replay();

        RequestSecurityManager manager
                = new RequestSecurityManagerImpl(request, response, linkFactory, locator, source, cache);

        assertFalse(manager.checkForInsecureRequest(PAGE_NAME));

        verify();
    }

    @Test
    public void check_page_not_secure() throws Exception
    {
        Request request = mockRequest();
        Response response = mockResponse();
        LinkFactory linkFactory = mockLinkFactory();
        MetaDataLocator locator = mockMetaDataLocator();
        BaseURLSource source = mockBaseURLSource();
        RequestPageCache cache = mockRequestPageCache();
        Page page = mockPage();

        train_isSecure(request, false);

        train_get(cache, PAGE_NAME, page);

        train_isSecure(locator, page, false);

        replay();

        RequestSecurityManager manager
                = new RequestSecurityManagerImpl(request, response, linkFactory, locator, source, cache);

        assertFalse(manager.checkForInsecureRequest(PAGE_NAME));

        verify();
    }

    @Test
    public void check_redirect_needed() throws Exception
    {
        Request request = mockRequest();
        Response response = mockResponse();
        LinkFactory linkFactory = mockLinkFactory();
        MetaDataLocator locator = mockMetaDataLocator();
        BaseURLSource source = mockBaseURLSource();
        Page page = mockPage();
        Link link = mockLink();
        RequestPageCache cache = mockRequestPageCache();

        train_isSecure(request, false);

        train_get(cache, PAGE_NAME, page);

        train_isSecure(locator, page, true);

        train_createPageLink(linkFactory, page, link);

        response.sendRedirect(link);

        replay();

        RequestSecurityManager manager
                = new RequestSecurityManagerImpl(request, response, linkFactory, locator, source, cache);

        assertTrue(manager.checkForInsecureRequest(PAGE_NAME));

        verify();
    }

    @DataProvider(name = "base_URL_data")
    public Object[][] base_URL_data()
    {
        return new Object[][] {
                { true, true, null },
                { false, false, null },
                { true, false, "http://example.org" },
                { false, true, "https://example.org" }
        };
    }

    @Test(dataProvider = "base_URL_data")
    public void get_base_URL(boolean secureRequest, boolean securePage, String expectedURL)
    {
        Request request = mockRequest();
        Response response = mockResponse();
        LinkFactory linkFactory = mockLinkFactory();
        MetaDataLocator locator = mockMetaDataLocator();
        BaseURLSource source = mockBaseURLSource();
        Page page = mockPage();

        train_isSecure(request, secureRequest);
        train_isSecure(locator, page, securePage);

        if (expectedURL != null)
            train_getBaseURL(source, securePage, expectedURL);

        replay();

        RequestSecurityManager manager
                = new RequestSecurityManagerImpl(request, response, linkFactory, locator, source, null);

        assertEquals(manager.getBaseURL(page), expectedURL);

        verify();
    }


    private void train_isSecure(MetaDataLocator locator, Page page, boolean secure)
    {
        Component component = mockComponent();
        ComponentResources resources = mockInternalComponentResources();

        train_getRootComponent(page, component);
        train_getComponentResources(component, resources);

        train_findMeta(locator, TapestryConstants.SECURE_PAGE, resources, Boolean.class, secure);
    }


}
