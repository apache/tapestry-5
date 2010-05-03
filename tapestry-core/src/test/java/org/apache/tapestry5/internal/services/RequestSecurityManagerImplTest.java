// Copyright 2008, 2009, 2010 The Apache Software Foundation
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

import org.apache.tapestry5.Link;
import org.apache.tapestry5.MetaDataConstants;
import org.apache.tapestry5.internal.EmptyEventContext;
import org.apache.tapestry5.internal.test.InternalBaseTestCase;
import org.apache.tapestry5.services.BaseURLSource;
import org.apache.tapestry5.services.ComponentEventLinkEncoder;
import org.apache.tapestry5.services.MetaDataLocator;
import org.apache.tapestry5.services.PageRenderRequestParameters;
import org.apache.tapestry5.services.Request;
import org.apache.tapestry5.services.Response;
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
        MetaDataLocator locator = mockMetaDataLocator();
        BaseURLSource source = mockBaseURLSource();
        ComponentEventLinkEncoder encoder = newMock(ComponentEventLinkEncoder.class);

        train_isSecure(request, true);

        replay();
        
        PageRenderRequestParameters parameters = new PageRenderRequestParameters(PAGE_NAME, new EmptyEventContext(), false);

        RequestSecurityManager manager
                = new RequestSecurityManagerImpl(request, response, encoder, locator, source, true);

        assertFalse(manager.checkForInsecurePageRenderRequest(parameters));

        verify();
    }

    @Test
    public void check_page_not_secure() throws Exception
    {
        Request request = mockRequest();
        Response response = mockResponse();
        MetaDataLocator locator = mockMetaDataLocator();
        BaseURLSource source = mockBaseURLSource();
        ComponentEventLinkEncoder encoder = newMock(ComponentEventLinkEncoder.class);

        train_isSecure(request, false);

        train_isSecure(locator, PAGE_NAME, false);

        replay();
        
        PageRenderRequestParameters parameters = new PageRenderRequestParameters(PAGE_NAME, new EmptyEventContext(), false);

        RequestSecurityManager manager
                = new RequestSecurityManagerImpl(request, response, encoder, locator, source, true);

        assertFalse(manager.checkForInsecurePageRenderRequest(parameters));

        verify();
    }

    @Test
    public void check_redirect_needed() throws Exception
    {
        Request request = mockRequest();
        Response response = mockResponse();
        MetaDataLocator locator = mockMetaDataLocator();
        BaseURLSource source = mockBaseURLSource();
        Link link = mockLink();
        ComponentEventLinkEncoder encoder = newMock(ComponentEventLinkEncoder.class);

        train_isSecure(request, false);

        train_isSecure(locator, PAGE_NAME, true);
        
        PageRenderRequestParameters parameters = new PageRenderRequestParameters(PAGE_NAME, new EmptyEventContext(), false);

        train_createPageRenderLink(encoder, parameters, link);

        response.sendRedirect(link);

        replay();

        RequestSecurityManager manager
                = new RequestSecurityManagerImpl(request, response, encoder, locator, source, true);

        assertTrue(manager.checkForInsecurePageRenderRequest(parameters));

        verify();
    }

    private void train_createPageRenderLink(ComponentEventLinkEncoder encoder, PageRenderRequestParameters parameters, Link link)
    {
        expect(encoder.createPageRenderLink(parameters)).andReturn(link);
    }

    @DataProvider
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
        MetaDataLocator locator = mockMetaDataLocator();
        BaseURLSource source = mockBaseURLSource();
        ComponentEventLinkEncoder encoder = newMock(ComponentEventLinkEncoder.class);

        train_isSecure(request, secureRequest);

        train_isSecure(locator, PAGE_NAME, securePage);

        if (expectedURL != null)
            train_getBaseURL(source, securePage, expectedURL);

        replay();

        RequestSecurityManager manager
                = new RequestSecurityManagerImpl(request, response, encoder, locator, source, true);

        assertEquals(manager.getBaseURL(PAGE_NAME), expectedURL);

        verify();
    }

    private static void train_isSecure(MetaDataLocator locator, String pageName, boolean securePage)
    {
        expect(locator.findMeta(MetaDataConstants.SECURE_PAGE, pageName, Boolean.class)).andReturn(securePage);
    }


}
