// Copyright 2006, 2007, 2008 The Apache Software Foundation
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
    private static final String OPTIMIZED = "/optimized/path";

    private static final String ENCODED = "*encoded*";


    @Test
    public void simple_redirect()
    {
        RequestPathOptimizer optimizer = mockRequestPathOptimizer();
        Response response = mockResponse();

        train_encodeRedirectURL(response, "/foo/bar", ENCODED);

        replay();

        Link link = new LinkImpl(response, optimizer, "/foo", "bar");


        assertEquals(link.toRedirectURI(), ENCODED);

        verify();
    }

    @Test
    public void to_string_same_as_to_uri()
    {
        RequestPathOptimizer optimizer = mockRequestPathOptimizer();
        Response response = mockResponse();

        train_optimizePath(optimizer, "/foo/bar", OPTIMIZED);
        train_encodeURL(response, OPTIMIZED, ENCODED);

        replay();

        Link link = new LinkImpl(response, optimizer, "/foo", "bar");


        assertEquals(link.toString(), ENCODED);

        verify();
    }

    @Test
    public void url_with_parameters()
    {
        RequestPathOptimizer optimizer = mockRequestPathOptimizer();
        Response response = mockResponse();

        train_optimizePath(optimizer, "/foo/bar?barney=rubble&fred=flintstone", OPTIMIZED);
        train_encodeURL(response, OPTIMIZED, ENCODED);

        replay();

        Link link = new LinkImpl(response, optimizer, "/foo", "bar");

        link.addParameter("fred", "flintstone");
        link.addParameter("barney", "rubble");

        assertEquals(link.toURI(), ENCODED);

        verify();
    }

    @Test
    public void retrieve_parameter_values()
    {
        RequestPathOptimizer optimizer = mockRequestPathOptimizer();
        Response response = mockResponse();

        replay();

        Link link = new LinkImpl(response, optimizer, "", "bar");

        link.addParameter("fred", "flintstone");
        link.addParameter("barney", "rubble");

        assertEquals(link.getParameterValue("fred"), "flintstone");
        assertEquals(link.getParameterValue("barney"), "rubble");
        assertNull(link.getParameterValue("wilma"));

        verify();
    }

    @Test
    public void parameter_names_must_be_unique()
    {
        RequestPathOptimizer optimizer = mockRequestPathOptimizer();
        Response response = mockResponse();

        replay();

        Link link = new LinkImpl(response, optimizer, "/foo", "bar");

        link.addParameter("fred", "flintstone");
        try
        {
            link.addParameter("fred", "flintstone");
            unreachable();
        }
        catch (IllegalArgumentException ex)
        {
            assertEquals(ex.getMessage(),
                         "Parameter names are required to be unique.  Parameter 'fred' already has the value 'flintstone'.");
        }

        verify();
    }

    @Test
    public void to_form_URI_does_not_include_parameters()
    {
        RequestPathOptimizer optimizer = mockRequestPathOptimizer();
        Response response = mockResponse();

        train_optimizePath(optimizer, "/foo/bar", OPTIMIZED);
        train_encodeURL(response, OPTIMIZED, ENCODED);

        replay();

        Link link = new LinkImpl(response, optimizer, "/foo", "bar", true);

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
        RequestPathOptimizer optimizer = mockRequestPathOptimizer();
        Response response = mockResponse();


        train_optimizePath(optimizer, url, OPTIMIZED);
        train_encodeURL(response, OPTIMIZED, ENCODED);


        replay();

        Link link = new LinkImpl(response, optimizer, "/foo", "bar");

        link.setAnchor(anchor);

        assertSame(link.getAnchor(), anchor);

        assertEquals(link.toURI(), ENCODED);

        verify();
    }

    @Test
    public void url_with_anchor_and_parameters()
    {
        RequestPathOptimizer optimizer = mockRequestPathOptimizer();
        Response response = mockResponse();


        train_optimizePath(optimizer, "/foo/bar?barney=rubble&fred=flintstone#wilma", OPTIMIZED);
        train_encodeURL(response, OPTIMIZED, ENCODED);

        replay();

        Link link = new LinkImpl(response, optimizer, "/foo", "bar");

        link.addParameter("fred", "flintstone");
        link.addParameter("barney", "rubble");
        link.setAnchor("wilma");

        assertEquals(link.toURI(), ENCODED);

        verify();
    }


    @Test
    public void force_absolute_uri()
    {
        RequestPathOptimizer optimizer = mockRequestPathOptimizer();
        Response response = mockResponse();


        train_encodeURL(response, "/ctx/foo", ENCODED);

        replay();


        Link link = new LinkImpl(response, optimizer, null, "/ctx",
                                 new ComponentInvocationImpl(new OpaqueConstantTarget("foo"), new String[0], null),
                                 false);

        assertEquals(link.toAbsoluteURI(), ENCODED);


        verify();
    }

    @Test
    public void parameter_names_are_returned_sorted()
    {
        RequestPathOptimizer optimizer = mockRequestPathOptimizer();
        Response response = mockResponse();

        replay();

        Link link = new LinkImpl(response, optimizer, "/foo", "bar");

        link.addParameter("fred", "flintstone");
        link.addParameter("barney", "rubble");

        assertEquals(link.getParameterNames(), Arrays.asList("barney", "fred"));

        verify();
    }
}
