// Copyright 2006 The Apache Software Foundation
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

import java.util.Arrays;

import org.apache.tapestry.Link;
import org.apache.tapestry.internal.test.InternalBaseTestCase;
import org.apache.tapestry.services.Response;
import org.testng.annotations.Test;

public class LinkImplTest extends InternalBaseTestCase
{
    private static final String ENCODED = "*encoded*";

    @Test
    public void url_with_parameters()
    {
        Response response = newResponse();

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
        Response response = newResponse();

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
    public void ensure_parameter_values_are_encoded()
    {
        Response response = newResponse();

        train_encodeURL(response, "/foo/bar?fred=flint+stone%3F", ENCODED);

        replay();

        Link link = new LinkImpl(response, "/foo", "bar");

        link.addParameter("fred", "flint stone?");

        assertEquals(link.toURI(), ENCODED);

        verify();
    }

    @Test
    public void parameter_names_are_returned_sorted()
    {
        Response response = newResponse();

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
        Response response = newResponse();

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
        Response response = newResponse();

        train_encodeURL(response, "/foo/bar", ENCODED);

        replay();

        Link link = new LinkImpl(response, "/foo", "bar", true);

        link.addParameter("fred", "flintstone");
        link.addParameter("barney", "rubble");

        assertEquals(link.toURI(), ENCODED);

        verify();
    }
}
