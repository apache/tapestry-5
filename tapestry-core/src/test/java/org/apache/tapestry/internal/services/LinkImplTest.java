// Copyright 2006, 2007 The Apache Software Foundation
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

import org.apache.tapestry.Link;
import org.apache.tapestry.internal.test.InternalBaseTestCase;
import org.apache.tapestry.services.Response;
import org.testng.annotations.Test;

import java.util.Arrays;

public class LinkImplTest extends InternalBaseTestCase
{
    private static final String ENCODED = "*encoded*";

    @Test
    public void url_with_parameters()
    {
        Response response = mockResponse();

        train_encodeURL(response, "/foo/bar?barney=rubble&fred=flintstone", ENCODED);

        replay();

        Link link = new LinkImpl(response, "/foo", "bar");

        link.addParameter("fred", "flintstone");
        link.addParameter("barney", "rubble");

        assertEquals(link.toURI(), ENCODED);

        verify();
    }

    @Test
    public void retrieve_parameter_values()
    {
        Response response = mockResponse();

        replay();

        Link link = new LinkImpl(response, "/foo", "bar");

        link.addParameter("fred", "flintstone");
        link.addParameter("barney", "rubble");

        assertEquals(link.getParameterValue("fred"), "flintstone");
        assertEquals(link.getParameterValue("barney"), "rubble");
        assertNull(link.getParameterValue("wilma"));

        verify();
    }

    @Test
    public void parameter_names_are_returned_sorted()
    {
        Response response = mockResponse();

        replay();

        Link link = new LinkImpl(response, "/foo", "bar");

        link.addParameter("fred", "flintstone");
        link.addParameter("barney", "rubble");

        assertEquals(link.getParameterNames(), Arrays.asList("barney", "fred"));

        verify();
    }

    @Test
    public void parameter_names_must_be_unique()
    {
        Response response = mockResponse();

        replay();

        Link link = new LinkImpl(response, "/foo", "bar");

        link.addParameter("fred", "flintstone");
        try
        {
            link.addParameter("fred", "flintstone");
            unreachable();
        }
        catch (IllegalArgumentException ex)
        {
            assertEquals(
                    ex.getMessage(),
                    "Parameter names are required to be unique.  Parameter 'fred' already has the value 'flintstone'.");
        }

        verify();
    }

    @Test
    public void to_form_URI_does_not_include_parameters()
    {
        Response response = mockResponse();

        train_encodeURL(response, "/foo/bar", ENCODED);

        replay();

        Link link = new LinkImpl(response, "/foo", "bar", true);

        link.addParameter("fred", "flintstone");
        link.addParameter("barney", "rubble");

        assertEquals(link.toURI(), ENCODED);

        verify();
    }

    @Test
    public void url_with_anchor()
    {
        url_with_anchor("wilma", "/foo/bar#wilma");
    }

    @Test
    public void url_with_null_anchor()
    {
        url_with_anchor(null, "/foo/bar");
    }

    @Test
    public void url_with_empty_anchor()
    {
        url_with_anchor("", "/foo/bar");
    }

    private void url_with_anchor(String anchor, String url)
    {
        Response response = mockResponse();

        train_encodeURL(response, url, ENCODED);

        replay();

        Link link = new LinkImpl(response, "/foo", "bar");

        link.setAnchor(anchor);

        assertEquals(link.toURI(), ENCODED);

        verify();
    }

    @Test
    public void url_with_anchor_and_parameters()
    {
        Response response = mockResponse();

        train_encodeURL(response, "/foo/bar?barney=rubble&fred=flintstone#wilma", ENCODED);

        replay();

        Link link = new LinkImpl(response, "/foo", "bar");

        link.addParameter("fred", "flintstone");
        link.addParameter("barney", "rubble");
        link.setAnchor("wilma");

        assertEquals(link.toURI(), ENCODED);

        verify();
    }

}
