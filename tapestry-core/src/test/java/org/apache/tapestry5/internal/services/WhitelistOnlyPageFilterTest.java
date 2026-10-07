// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.apache.tapestry5.internal.services;

import org.apache.tapestry5.EventConstants;
import org.apache.tapestry5.MetaDataConstants;
import org.apache.tapestry5.http.services.Request;
import org.apache.tapestry5.internal.EmptyEventContext;
import org.apache.tapestry5.internal.InternalConstants;
import org.apache.tapestry5.internal.test.InternalBaseTestCase;
import org.apache.tapestry5.services.ComponentEventRequestParameters;
import org.apache.tapestry5.services.ComponentRequestHandler;
import org.apache.tapestry5.services.MetaDataLocator;
import org.apache.tapestry5.services.PageRenderRequestParameters;
import org.apache.tapestry5.services.security.ClientWhitelist;
import org.testng.annotations.Test;

/**
 * The whitelist gate that normally rejects these requests lives in
 * {@link ComponentEventLinkEncoderImpl}; this filter repeats it independently, so that any other
 * route into the component event pipeline is covered too.
 */
public class WhitelistOnlyPageFilterTest extends InternalBaseTestCase
{
    private static final String PUBLIC_PAGE = "PublicPage";

    private static final String PROTECTED_PAGE = "core/PageCatalog";

    private static ComponentEventRequestParameters parameters(String activePageName, String containingPageName)
    {
        return new ComponentEventRequestParameters(activePageName, containingPageName, "", EventConstants.ACTION,
                new EmptyEventContext(), new EmptyEventContext());
    }

    private void train_isWhitelistOnly(MetaDataLocator locator, String pageName, boolean whitelistOnly)
    {
        expect(locator.findMeta(MetaDataConstants.WHITELIST_ONLY_PAGE, pageName, boolean.class))
                .andReturn(whitelistOnly);
    }

    @Test
    public void whitelist_only_active_page_is_not_handled() throws Exception
    {
        Request request = mockRequest();
        MetaDataLocator locator = mockMetaDataLocator();
        ClientWhitelist whitelist = newMock(ClientWhitelist.class);
        ComponentRequestHandler handler = mockComponentRequestHandler();

        train_isWhitelistOnly(locator, PROTECTED_PAGE, true);

        expect(whitelist.isClientRequestOnWhitelist()).andReturn(false);

        // Handling is aborted; the dispatcher turns this into a 404.

        request.setAttribute(InternalConstants.REFERENCED_COMPONENT_NOT_FOUND, true);

        replay();

        new WhitelistOnlyPageFilter(request, locator, whitelist)
                .handleComponentEvent(parameters(PROTECTED_PAGE, PROTECTED_PAGE), handler);

        verify();
    }

    /**
     * The exploit the encoder's check closes: a public active page in the URL path fronting a
     * whitelist-only containing page named by the t:cp request parameter.
     */
    @Test
    public void whitelist_only_containing_page_is_not_handled() throws Exception
    {
        Request request = mockRequest();
        MetaDataLocator locator = mockMetaDataLocator();
        ClientWhitelist whitelist = newMock(ClientWhitelist.class);
        ComponentRequestHandler handler = mockComponentRequestHandler();

        train_isWhitelistOnly(locator, PUBLIC_PAGE, false);

        train_isWhitelistOnly(locator, PROTECTED_PAGE, true);

        expect(whitelist.isClientRequestOnWhitelist()).andReturn(false);

        request.setAttribute(InternalConstants.REFERENCED_COMPONENT_NOT_FOUND, true);

        replay();

        new WhitelistOnlyPageFilter(request, locator, whitelist)
                .handleComponentEvent(parameters(PUBLIC_PAGE, PROTECTED_PAGE), handler);

        verify();
    }

    @Test
    public void whitelist_only_page_is_handled_for_a_client_on_the_whitelist() throws Exception
    {
        Request request = mockRequest();
        MetaDataLocator locator = mockMetaDataLocator();
        ClientWhitelist whitelist = newMock(ClientWhitelist.class);
        ComponentRequestHandler handler = mockComponentRequestHandler();

        ComponentEventRequestParameters parameters = parameters(PUBLIC_PAGE, PROTECTED_PAGE);

        train_isWhitelistOnly(locator, PUBLIC_PAGE, false);

        train_isWhitelistOnly(locator, PROTECTED_PAGE, true);

        expect(whitelist.isClientRequestOnWhitelist()).andReturn(true);

        handler.handleComponentEvent(parameters);

        replay();

        new WhitelistOnlyPageFilter(request, locator, whitelist).handleComponentEvent(parameters, handler);

        verify();
    }

    @Test
    public void unprotected_pages_are_handled() throws Exception
    {
        Request request = mockRequest();
        MetaDataLocator locator = mockMetaDataLocator();
        ClientWhitelist whitelist = newMock(ClientWhitelist.class);
        ComponentRequestHandler handler = mockComponentRequestHandler();

        ComponentEventRequestParameters parameters = parameters(PUBLIC_PAGE, PUBLIC_PAGE);

        train_isWhitelistOnly(locator, PUBLIC_PAGE, false);

        train_isWhitelistOnly(locator, PUBLIC_PAGE, false);

        handler.handleComponentEvent(parameters);

        replay();

        new WhitelistOnlyPageFilter(request, locator, whitelist).handleComponentEvent(parameters, handler);

        verify();
    }

    /**
     * Page render requests are gated while decoding; the page render dispatcher does not honour the
     * attribute this filter sets, so they are passed straight through.
     */
    @Test
    public void page_render_requests_are_passed_through() throws Exception
    {
        Request request = mockRequest();
        MetaDataLocator locator = mockMetaDataLocator();
        ClientWhitelist whitelist = newMock(ClientWhitelist.class);
        ComponentRequestHandler handler = mockComponentRequestHandler();

        PageRenderRequestParameters parameters = new PageRenderRequestParameters(PROTECTED_PAGE,
                new EmptyEventContext(), false);

        handler.handlePageRender(parameters);

        replay();

        new WhitelistOnlyPageFilter(request, locator, whitelist).handlePageRender(parameters, handler);

        verify();
    }
}
