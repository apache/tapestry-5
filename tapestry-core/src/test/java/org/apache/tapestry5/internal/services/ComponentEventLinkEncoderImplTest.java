// Copyright 2009-2013 The Apache Software Foundation
//
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

import org.apache.tapestry5.MetaDataConstants;
import org.apache.tapestry5.TapestryConstants;
import org.apache.tapestry5.beanmodel.services.*;
import org.apache.tapestry5.commons.services.TypeCoercer;
import org.apache.tapestry5.http.Link;
import org.apache.tapestry5.http.LinkSecurity;
import org.apache.tapestry5.http.services.Request;
import org.apache.tapestry5.http.services.Response;
import org.apache.tapestry5.internal.EmptyEventContext;
import org.apache.tapestry5.internal.InternalConstants;
import org.apache.tapestry5.internal.test.InternalBaseTestCase;
import org.apache.tapestry5.services.ComponentClassResolver;
import org.apache.tapestry5.services.ComponentEventLinkEncoder;
import org.apache.tapestry5.services.ComponentEventRequestParameters;
import org.apache.tapestry5.services.ContextPathEncoder;
import org.apache.tapestry5.services.LocalizationSetter;
import org.apache.tapestry5.services.MetaDataLocator;
import org.apache.tapestry5.services.PageRenderRequestParameters;
import org.apache.tapestry5.services.security.ClientWhitelist;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Locale;

/**
 * Most of the testing is implemented through legacy tests against code that uses CELE.
 *
 * @since 5.1.0.1
 */
public class ComponentEventLinkEncoderImplTest extends InternalBaseTestCase
{
    private TypeCoercer typeCoercer;

    private ContextPathEncoder contextPathEncoder;

    @BeforeClass
    public void setup()
    {
        typeCoercer = getService(TypeCoercer.class);
        contextPathEncoder = getService(ContextPathEncoder.class);
    }

    @Test
    public void locale_not_encoded()
    {
        RequestSecurityManager manager = mockRequestSecurityManager();
        Response response = mockResponse();
        ContextPathEncoder contextPathEncoder = getService(ContextPathEncoder.class);

        expect(manager.checkPageSecurity("MyPage")).andReturn(LinkSecurity.INSECURE);

        train_encodeURL(response, "/myapp/mypage", "MAGIC");

        replay();

        ComponentEventLinkEncoder encoder = new ComponentEventLinkEncoderImpl(null, contextPathEncoder, null,
                response, manager, null, null, false, "/myapp", "", null, null);

        PageRenderRequestParameters parameters = new PageRenderRequestParameters("MyPage", new EmptyEventContext());

        Link link = encoder.createPageRenderLink(parameters);

        assertEquals(link.toURI(), "MAGIC");

        verify();
    }

    @Test
    public void index_stripped_off()
    {
        RequestSecurityManager manager = mockRequestSecurityManager();
        Response response = mockResponse();
        ContextPathEncoder contextPathEncoder = getService(ContextPathEncoder.class);

        expect(manager.checkPageSecurity("admin/Index")).andReturn(LinkSecurity.INSECURE);

        train_encodeURL(response, "/admin/abc", "MAGIC");

        replay();

        ComponentEventLinkEncoder encoder = new ComponentEventLinkEncoderImpl(null, contextPathEncoder, null,
                response, manager, null, null, false, "", "", null, null);

        PageRenderRequestParameters parameters = new PageRenderRequestParameters("admin/Index", new ArrayEventContext(
                typeCoercer, "abc"));

        Link link = encoder.createPageRenderLink(parameters);

        assertEquals(link.toURI(), "MAGIC");

        verify();
    }

    @Test
    public void root_index_page_gone()
    {
        RequestSecurityManager manager = mockRequestSecurityManager();
        Response response = mockResponse();
        ContextPathEncoder contextPathEncoder = getService(ContextPathEncoder.class);

        expect(manager.checkPageSecurity("Index")).andReturn(LinkSecurity.INSECURE);

        train_encodeURL(response, "/", "MAGIC");

        replay();

        ComponentEventLinkEncoder encoder = new ComponentEventLinkEncoderImpl(null, contextPathEncoder, null,
                response, manager, null, null, false, "", "", null, null);

        PageRenderRequestParameters parameters = new PageRenderRequestParameters("Index", new EmptyEventContext());

        Link link = encoder.createPageRenderLink(parameters);

        assertEquals(link.toURI(), "MAGIC");

        verify();
    }

    @Test
    public void empty_path() throws Exception
    {
        ComponentClassResolver resolver = mockComponentClassResolver();
        Request request = mockRequest();
        Response response = mockResponse();
        LocalizationSetter ls = mockLocalizationSetter();

        train_getPath(request, "");

        train_setLocaleFromLocaleName(ls, "", false);

        train_isPageName(resolver, "", false);

        replay();

        ComponentEventLinkEncoderImpl linkEncoder = new ComponentEventLinkEncoderImpl(resolver, contextPathEncoder, ls,
                response, null, null, null, true, null, "", null, null);

        PageRenderRequestParameters parameters = linkEncoder.decodePageRenderRequest(request);

        assertNull(parameters);

        verify();
    }

    @Test
    public void not_a_page_request() throws Exception
    {
        ComponentClassResolver resolver = mockComponentClassResolver();
        Request request = mockRequest();
        Response response = mockResponse();
        LocalizationSetter ls = mockLocalizationSetter();

        stub_isPageName(resolver, false);

        train_setLocaleFromLocaleName(ls, "foo", false);
        train_getPath(request, "/foo/Bar.baz");

        replay();

        ComponentEventLinkEncoderImpl linkEncoder = new ComponentEventLinkEncoderImpl(resolver, contextPathEncoder, ls,
                response, null, null, null, true, null, "", null, null);

        PageRenderRequestParameters parameters = linkEncoder.decodePageRenderRequest(request);

        assertNull(parameters);

        verify();
    }

    @Test
    public void just_the_locale_name() throws Exception
    {
        ComponentClassResolver resolver = mockComponentClassResolver();
        Request request = mockRequest();
        Response response = mockResponse();
        LocalizationSetter ls = mockLocalizationSetter();

        train_getPath(request, "/en");

        train_setLocaleFromLocaleName(ls, "en", true);

        train_isPageName(resolver, "", false);

        replay();

        ComponentEventLinkEncoderImpl linkEncoder = new ComponentEventLinkEncoderImpl(resolver, contextPathEncoder, ls,
                response, null, null, null, true, null, "", null, null);

        PageRenderRequestParameters parameters = linkEncoder.decodePageRenderRequest(request);

        assertNull(parameters);

        verify();
    }

    private Request mockRequest(boolean isLoopback)
    {
        Request request = mockRequest();

        train_getParameter(request, TapestryConstants.PAGE_LOOPBACK_PARAMETER_NAME, isLoopback ? "t" : null);

        return request;
    }

    /**
     * TAPESTRY-2226
     */
    @Test
    public void page_activation_context_for_root_index_page() throws Exception
    {
        ComponentClassResolver resolver = mockComponentClassResolver();
        Request request = mockRequest(false);
        LocalizationSetter ls = mockLocalizationSetter();
        MetaDataLocator metaDataLocator = neverWhitelistProtected();

        train_getPath(request, "/foo/bar");

        train_setLocaleFromLocaleName(ls, "foo", false);

        train_isPageName(resolver, "foo/bar", false);
        train_isPageName(resolver, "foo", false);
        train_isPageName(resolver, "", true);

        train_canonicalizePageName(resolver, "", "index");

        train_getLocale(request, Locale.ITALIAN);
        ls.setNonPersistentLocaleFromLocaleName("it");

        replay();

        ComponentEventLinkEncoderImpl linkEncoder = new ComponentEventLinkEncoderImpl(resolver, contextPathEncoder, ls,
                null, null, null, null, true, null, "", metaDataLocator, null);

        PageRenderRequestParameters parameters = linkEncoder.decodePageRenderRequest(request);

        assertEquals(parameters.getLogicalPageName(), "index");
        assertArraysEqual(parameters.getActivationContext().toStrings(), "foo", "bar");
        assertFalse(parameters.isLoopback());

        verify();
    }

    @Test
    public void no_extra_context_without_final_slash() throws Exception
    {
        no_extra_context(false);
    }

    @Test
    public void no_extra_context_with_final_slash() throws Exception
    {
        no_extra_context(true);
    }

    private void no_extra_context(boolean finalSlash) throws Exception
    {
        ComponentClassResolver resolver = mockComponentClassResolver();
        Request request = mockRequest(false);
        LocalizationSetter ls = mockLocalizationSetter();
        MetaDataLocator metaDataLocator = neverWhitelistProtected();

        String path = "/foo/Bar" + (finalSlash ? "/" : "");
        train_getPath(request, path);

        train_setLocaleFromLocaleName(ls, "foo", false);

        train_isPageName(resolver, "foo/Bar", true);

        train_canonicalizePageName(resolver, "foo/Bar", "foo/bar");

        replay();

        ComponentEventLinkEncoderImpl linkEncoder = new ComponentEventLinkEncoderImpl(resolver, contextPathEncoder, ls,
                null, null, null, null, true, null, "", metaDataLocator, null);

        PageRenderRequestParameters parameters = linkEncoder.decodePageRenderRequest(request);

        assertEquals(parameters.getLogicalPageName(), "foo/bar");
        assertEquals(parameters.getActivationContext().getCount(), 0);
        assertFalse(parameters.isLoopback());

        verify();
    }

    @Test
    public void page_requires_whitelist_and_client_on_whitelist() throws Exception
    {
        ComponentClassResolver resolver = mockComponentClassResolver();
        Request request = mockRequest(false);
        LocalizationSetter ls = mockLocalizationSetter();
        MetaDataLocator metaDataLocator = mockMetaDataLocator();
        ClientWhitelist whitelist = newMock(ClientWhitelist.class);

        String path = "/foo/Bar";

        train_getPath(request, path);

        train_setLocaleFromLocaleName(ls, "foo", false);

        train_isPageName(resolver, "foo/Bar", true);

        train_canonicalizePageName(resolver, "foo/Bar", "foo/bar");

        expect(metaDataLocator.findMeta(MetaDataConstants.WHITELIST_ONLY_PAGE, "foo/bar", boolean.class)).andReturn(true);
        expect(whitelist.isClientRequestOnWhitelist()).andReturn(true);

        replay();

        ComponentEventLinkEncoderImpl linkEncoder = new ComponentEventLinkEncoderImpl(resolver, contextPathEncoder, ls,
                null, null, null, null, true, null, "", metaDataLocator, whitelist);

        PageRenderRequestParameters parameters = linkEncoder.decodePageRenderRequest(request);

        assertEquals(parameters.getLogicalPageName(), "foo/bar");
        assertEquals(parameters.getActivationContext().getCount(), 0);
        assertFalse(parameters.isLoopback());

        verify();
    }

    @Test
    public void page_requires_whitelist_and_client_not_on_whitelist()
    {
        ComponentClassResolver resolver = mockComponentClassResolver();
        Request request = mockRequest();
        LocalizationSetter ls = mockLocalizationSetter();
        MetaDataLocator metaDataLocator = mockMetaDataLocator();
        ClientWhitelist whitelist = newMock(ClientWhitelist.class);

        String path = "/foo/Bar";

        train_getPath(request, path);

        train_setLocaleFromLocaleName(ls, "foo", false);

        train_isPageName(resolver, "foo/Bar", true);

        train_canonicalizePageName(resolver, "foo/Bar", "foo/bar");

        expect(metaDataLocator.findMeta(MetaDataConstants.WHITELIST_ONLY_PAGE, "foo/bar", boolean.class)).andReturn(true);
        expect(whitelist.isClientRequestOnWhitelist()).andReturn(false);

        train_isPageName(resolver, "foo", false);
        train_isPageName(resolver, "", false);

        replay();

        ComponentEventLinkEncoderImpl linkEncoder = new ComponentEventLinkEncoderImpl(resolver, contextPathEncoder, ls,
                null, null, null, null, true, null, "", metaDataLocator, whitelist);

        assertNull(linkEncoder.decodePageRenderRequest(request));

        verify();
    }

    @Test
    public void context_passed_in_path_without_final_slash() throws Exception
    {
        context_passed_in_path(false);
    }

    @Test
    public void context_passed_in_path_with_final_slash() throws Exception
    {
        context_passed_in_path(true);
    }

    private void context_passed_in_path(boolean finalSlash) throws Exception
    {
        ComponentClassResolver resolver = mockComponentClassResolver();
        Request request = mockRequest(true);
        LocalizationSetter ls = mockLocalizationSetter();
        MetaDataLocator metaDataLocator = neverWhitelistProtected();

        String path = "/foo/Bar/zip/zoom" + (finalSlash ? "/" : "");
        train_getPath(request, path);

        train_setLocaleFromLocaleName(ls, "foo", false);

        train_isPageName(resolver, "foo/Bar/zip/zoom", false);

        train_isPageName(resolver, "foo/Bar/zip", false);

        train_isPageName(resolver, "foo/Bar", true);

        train_canonicalizePageName(resolver, "foo/Bar", "foo/bar");

        replay();

        ComponentEventLinkEncoderImpl linkEncoder = new ComponentEventLinkEncoderImpl(resolver, contextPathEncoder, ls,
                null, null, null, null, true, null, "", metaDataLocator, null);

        PageRenderRequestParameters parameters = linkEncoder.decodePageRenderRequest(request);

        assertEquals(parameters.getLogicalPageName(), "foo/bar");
        assertArraysEqual(parameters.getActivationContext().toStrings(), "zip", "zoom");
        assertTrue(parameters.isLoopback());

        verify();
    }

    @Test
    public void page_name_includes_dash_in_component_event_request()
    {
        ComponentClassResolver resolver = mockComponentClassResolver();
        Request request = mockRequest();
        LocalizationSetter ls = mockLocalizationSetter();
        MetaDataLocator metaDataLocator = neverWhitelistProtected();

        expect(ls.isSupportedLocaleName("foo-bar")).andReturn(false);

        train_getParameter(request, InternalConstants.PAGE_CONTEXT_NAME, null);
        train_getParameter(request, InternalConstants.CONTAINER_PAGE_NAME, null);
        train_getLocale(request, Locale.ENGLISH);

        ls.setNonPersistentLocaleFromLocaleName("en");

        String path = "/foo-bar/baz.biff";
        train_getPath(request, path);

        train_isPageName(resolver, "foo-bar/baz", true);

        train_canonicalizePageName(resolver, "foo-bar/baz", "foo-bar/Baz");

        replay();

        ComponentEventLinkEncoderImpl linkEncoder = new ComponentEventLinkEncoderImpl(resolver, contextPathEncoder, ls,
                null, null, null, null, true, null, "", metaDataLocator, null);

        ComponentEventRequestParameters parameters = linkEncoder.decodeComponentEventRequest(request);

        assertEquals(parameters.getActivePageName(), "foo-bar/Baz");
        assertEquals(parameters.getNestedComponentId(), "biff");

        verify();

    }

    @Test
    public void decode_compoent_event_request_with_slash_in_context_path() throws Exception
    {
        ComponentClassResolver resolver = mockComponentClassResolver();
        Request request = mockRequest();
        Response response = mockResponse();
        LocalizationSetter ls = mockLocalizationSetter();
        MetaDataLocator metaDataLocator = neverWhitelistProtected();

        expect(ls.isSupportedLocaleName("page.component:event")).andReturn(false);

        train_getParameter(request, InternalConstants.PAGE_CONTEXT_NAME, null);
        train_getParameter(request, InternalConstants.CONTAINER_PAGE_NAME, null);
        train_getLocale(request, Locale.ENGLISH);

        ls.setNonPersistentLocaleFromLocaleName("en");

        train_getPath(request, "/foo/bar/page.component:event");

        train_isPageName(resolver, "page", true);

        train_canonicalizePageName(resolver, "page", "Page");

        replay();

        ComponentEventLinkEncoderImpl linkEncoder = new ComponentEventLinkEncoderImpl(resolver, contextPathEncoder, ls,
                response, null, null, null, true, null, "foo/bar", metaDataLocator, null);

        ComponentEventRequestParameters parameters = linkEncoder.decodeComponentEventRequest(request);
        assertNotNull(parameters);
        assertEquals(parameters.getActivePageName(), "Page");
        assertEquals(parameters.getEventType(), "event");

        verify();
    }

    @Test
    // TAP5-2436
    public void illegal_activation_context_leads_to_http_404() throws Exception
    {
        ComponentClassResolver resolver = mockComponentClassResolver();
        Request request = mockRequest();
        Response response = mockResponse();
        LocalizationSetter ls = mockLocalizationSetter();
        MetaDataLocator metaDataLocator = neverWhitelistProtected();

        train_getPath(request, "/foo/pageid=123");
        train_setLocaleFromLocaleName(ls, "foo", false);

        train_isPageName(resolver, "foo/pageid=123", false);
        train_isPageName(resolver, "foo", false);
        train_isPageName(resolver, "", true);

        train_canonicalizePageName(resolver, "", "Index");

        replay();

        ComponentEventLinkEncoderImpl linkEncoder = new ComponentEventLinkEncoderImpl(resolver, contextPathEncoder, ls,
                response, null, null, null, true, null, "", metaDataLocator, null);

        PageRenderRequestParameters parameters = linkEncoder.decodePageRenderRequest(request);

        assertNull(parameters);

        verify();
    }
}
