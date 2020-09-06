// Copyright 2006-2013 The Apache Software Foundation
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

import org.apache.tapestry5.http.Link;
import org.apache.tapestry5.http.LinkSecurity;
import org.apache.tapestry5.http.services.BaseURLSource;
import org.apache.tapestry5.http.services.Response;
import org.apache.tapestry5.internal.test.InternalBaseTestCase;
import org.apache.tapestry5.services.ContextPathEncoder;
import org.testng.annotations.Test;

public class LinkImplTest extends InternalBaseTestCase
{
    private static final String BASE_PATH = "/bar/baz";

    private static final String RAW_PATH = "foo/baz/path";

    private static final String ENCODED = "*encoded*";

    private static final String INSECURE_BASE_URL = "http://insecure.com";

    private static final String SECURE_BASE_URL = "https://secure.com";

    @Test
    public void simple_redirect()
    {
        Response response = mockResponse();

        String URI = "/base/context/" + RAW_PATH;

        train_encodeRedirectURL(response, URI, ENCODED);

        replay();

        Link link = new LinkImpl(URI, false, LinkSecurity.INSECURE, response, null, null);

        assertEquals(link.toRedirectURI(), ENCODED);

        verify();
    }

    @Test
    public void absolute_URI_for_default_insecure_link()
    {
        Response response = mockResponse();
        BaseURLSource baseURLSource = mockBaseURLSource();

        train_getBaseURL(baseURLSource, false, INSECURE_BASE_URL);

        train_encodeURL(response, INSECURE_BASE_URL + BASE_PATH, ENCODED);

        replay();

        Link link = new LinkImpl(BASE_PATH, false, LinkSecurity.INSECURE, response, null, baseURLSource);

        assertEquals(link.toAbsoluteURI(), ENCODED);

        verify();
    }

    @Test
    public void absolute_URI_for_default_secure_link()
    {
        Response response = mockResponse();
        BaseURLSource baseURLSource = mockBaseURLSource();

        train_getBaseURL(baseURLSource, true, SECURE_BASE_URL);

        train_encodeURL(response, SECURE_BASE_URL + BASE_PATH, ENCODED);

        replay();

        Link link = new LinkImpl(BASE_PATH, false, LinkSecurity.SECURE, response, null, baseURLSource);

        assertEquals(link.toAbsoluteURI(), ENCODED);

        verify();
    }

    @Test
    public void force_secure_URI_from_insecure_link()
    {
        Response response = mockResponse();
        BaseURLSource baseURLSource = mockBaseURLSource();

        train_getBaseURL(baseURLSource, true, SECURE_BASE_URL);

        train_encodeURL(response, SECURE_BASE_URL + BASE_PATH, ENCODED);

        replay();

        Link link = new LinkImpl(BASE_PATH, false, LinkSecurity.INSECURE, response, null, baseURLSource);

        assertEquals(link.toAbsoluteURI(true), ENCODED);

        verify();
    }

    @Test
    public void force_insecure_URI_from_secure_link()
    {
        Response response = mockResponse();
        BaseURLSource baseURLSource = mockBaseURLSource();

        train_getBaseURL(baseURLSource, false, INSECURE_BASE_URL);

        train_encodeURL(response, INSECURE_BASE_URL + BASE_PATH, ENCODED);

        replay();

        Link link = new LinkImpl(BASE_PATH, false, LinkSecurity.SECURE, response, null, baseURLSource);

        assertEquals(link.toAbsoluteURI(false), ENCODED);

        verify();
    }

    @Test
    public void to_string_same_as_to_uri()
    {
        Response response = mockResponse();

        String url = "/bar/" + RAW_PATH;

        train_encodeURL(response, url, ENCODED);

        replay();

        Link link = new LinkImpl(url, false, LinkSecurity.INSECURE, response, null, null);

        assertEquals(link.toString(), ENCODED);

        assertEquals(link.getBasePath(), url);

        verify();
    }

    @Test
    public void retrieve_parameter_values()
    {
        Response response = mockResponse();

        replay();

        Link link = new LinkImpl("/foo/bar", false, null, response, null, null);

        link.addParameter("fred", "flintstone");

        assertEquals(link.getParameterValue("fred"), "flintstone");

        verify();
    }

    @Test
    public void url_with_anchor()
    {
        Response response = mockResponse();

        String url = "/foo/bar";

        train_encodeURL(response, url, ENCODED);

        replay();

        Link link = new LinkImpl(url, false, LinkSecurity.INSECURE, response, null, null);
        link.setAnchor("wilma");

        assertSame(link.getAnchor(), "wilma");

        assertEquals(link.toURI(), ENCODED + "#" + "wilma");

        verify();
    }

    @Test
    public void to_uri_with_added_parameters_and_on_construction_uri()
    {
        Response response = mockResponse();

        String expectedURI = "/ctx/foo?foo=bar&baz=barney";
        train_encodeURL(response, expectedURI, expectedURI);

        replay();

        Link link = new LinkImpl("/ctx/foo?foo=bar", false, LinkSecurity.INSECURE, response, null, null);
        link.addParameter("baz", "barney");

        assertEquals(link.toURI(), expectedURI);

        verify();
    }

    @Test
    public void add_parameter_value()
    {
        Response response = mockResponse();
        ContextPathEncoder encoder = newMock(ContextPathEncoder.class);

        expect(encoder.encodeValue("plain")).andReturn("encoded");

        String expectedURI = "/ctx/foo?bar=encoded";
        train_encodeURL(response, expectedURI, expectedURI);

        replay();

        Link link = new LinkImpl("/ctx/foo", false, LinkSecurity.INSECURE, response, encoder, null);

        assertSame(link.addParameterValue("bar", "plain"), link);

        assertEquals(link.toURI(), expectedURI);

        verify();
    }

    @Test
    public void new_base_uri()
    {
        Response response = mockResponse();

        String expectedURI = "/ctx/baz?baz=barney";
        train_encodeURL(response, expectedURI, expectedURI);

        replay();

        Link link = new LinkImpl("/ctx/foo", false, LinkSecurity.INSECURE, response, null, null);
        link.addParameter("baz", "barney");
        link.setAnchor("jacob");

        Link copy = link.copyWithBasePath("/ctx/baz");

        assertEquals(copy.toURI(), expectedURI + "#jacob");

        verify();
    }

    @Test
    public void remove_parameter()
    {
        Link link = new LinkImpl("/baseURI", false, null, null, null, null);

        link.addParameter("fred", "flintstone");
        link.addParameter("barney", "rubble");

        link.removeParameter("fred");

        assertNull(link.getParameterValue("fred"));
        assertListsEquals(link.getParameterNames(), "barney");
    }

    /**
     * TAP5-922
     *
     * @since 5.3
     */
    @Test
    public void null_parameter_value()
    {
        Response response = mockResponse();

        String expectedURI = "/ctx/foo?barney=&fred=flintstone";
        train_encodeURL(response, expectedURI, expectedURI);

        replay();

        Link link = new LinkImpl("/ctx/foo", false, LinkSecurity.INSECURE, response, null, null);

        link.addParameter("fred", "flintstone");
        link.addParameter("barney", null);

        assertEquals(link.toURI(), expectedURI);

        verify();
    }

    /**
     * TAP5-2063
     */
    @Test
    public void multivalued_parameter_support()
    {
        Response response = mockResponse();

        String expectedURI = "/ctx?barney=&barney=foo&barney=bar&barney=baz&fred=flintstone";
        train_encodeURL(response, expectedURI, expectedURI);

        replay();

        Link link = new LinkImpl("/ctx", false, LinkSecurity.INSECURE, response, null, null);

        link.addParameter("fred", "flintstone");
        link.addParameter("barney", null);
        link.addParameter("barney", "foo");
        link.addParameter("barney", "bar");
        link.addParameter("barney", "baz");

        assertEquals(link.toURI(), expectedURI);

        verify();
    }


    @Test
    public void force_link_to_secure()
    {
        Response response = mockResponse();
        BaseURLSource baseURLSource = mockBaseURLSource();

        train_getBaseURL(baseURLSource, true, SECURE_BASE_URL);

        train_encodeURL(response, SECURE_BASE_URL + BASE_PATH, ENCODED);

        replay();

        Link link = new LinkImpl(BASE_PATH, false, LinkSecurity.INSECURE, response, null, baseURLSource);

        link.setSecurity(LinkSecurity.FORCE_SECURE);

        assertEquals(link.toAbsoluteURI(), ENCODED);

        verify();
    }

}
