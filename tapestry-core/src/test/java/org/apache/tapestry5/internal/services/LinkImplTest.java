// Copyright 2006, 2007, 2008, 2009, 2010 The Apache Software Foundation
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

import org.apache.tapestry5.Link;
import org.apache.tapestry5.internal.test.InternalBaseTestCase;
import org.apache.tapestry5.services.Response;
import org.testng.annotations.Test;

public class LinkImplTest extends InternalBaseTestCase
{
    private static final String RAW_PATH = "foo/baz/path";

    private static final String OPTIMIZED = "../baz/path";

    private static final String ENCODED = "*encoded*";

    @Test
    public void simple_redirect()
    {
        Response response = mockResponse();

        String URI = "/base/context/" + RAW_PATH;

        train_encodeRedirectURL(response, URI, ENCODED);

        replay();

        Link link = new LinkImpl(URI, false, response);

        assertEquals(link.toRedirectURI(), ENCODED);

        verify();
    }

    @Test
    public void to_string_same_as_to_uri()
    {
        Response response = mockResponse();

        String url = "/bar/" + RAW_PATH;

        train_encodeURL(response, url, ENCODED);

        replay();

        Link link = new LinkImpl(url, false, response);

        assertEquals(link.toString(), ENCODED);

        verify();
    }

    @Test
    public void retrieve_parameter_values()
    {
        Response response = mockResponse();

        replay();

        Link link = new LinkImpl("/foo/bar", false, response);

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

        Link link = new LinkImpl(url, false, response);
        link.setAnchor("wilma");

        assertSame(link.getAnchor(), "wilma");

        assertEquals(link.toURI(), ENCODED + "#" + "wilma");

        verify();
    }

    @Test
    public void force_absolute_uri()
    {
        Response response = mockResponse();

        train_encodeURL(response, "/ctx/foo", ENCODED);

        replay();

        Link link = new LinkImpl("/ctx/foo", false, response);

        assertEquals(link.toAbsoluteURI(), ENCODED);

        verify();
    }

    @Test
    public void to_uri_with_added_parameters_and_on_construction_uri()
    {
        Response response = mockResponse();

        String expectedURI = "/ctx/foo?foo=bar&baz=barney";
        train_encodeURL(response, expectedURI, expectedURI);

        replay();

        Link link = new LinkImpl("/ctx/foo?foo=bar", false, response);
        link.addParameter("baz", "barney");

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

        Link link = new LinkImpl("/ctx/foo", false, response);
        link.addParameter("baz", "barney");
        link.setAnchor("jacob");

        Link copy = link.copyWithBasePath("/ctx/baz");

        assertEquals(copy.toURI(), expectedURI + "#jacob");

        verify();
    }

    @Test
    public void remove_parameter()
    {
        Link link = new LinkImpl("/baseURI", false, null);

        link.addParameter("fred", "flintstone");
        link.addParameter("barney", "rubble");

        link.removeParameter("fred");

        assertNull(link.getParameterValue("fred"));
        assertListsEquals(link.getParameterNames(), "barney");
    }
}
