// Copyright 2006, 2007, 2008, 2009 The Apache Software Foundation
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
import org.apache.tapestry5.internal.test.InternalBaseTestCase;
import org.apache.tapestry5.services.ContextPathEncoder;
import org.apache.tapestry5.services.Response;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class LinkImplTest extends InternalBaseTestCase
{
    private static final String RAW_PATH = "foo/baz/path";

    private static final String OPTIMIZED = "../baz/path";

    private static final String ENCODED = "*encoded*";

    private ContextPathEncoder contextPathEncoder;

    @BeforeClass
    public void setup()
    {
        contextPathEncoder = getService(ContextPathEncoder.class);
    }

    @Test
    public void simple_redirect()
    {
        RequestPathOptimizer optimizer = mockRequestPathOptimizer();
        Response response = mockResponse();

        String URI = "/base/context/" + RAW_PATH;

        train_encodeRedirectURL(response, URI, ENCODED);

        replay();

        Link link = new LinkImpl(URI, true, false, response, optimizer);

        assertEquals(link.toRedirectURI(), ENCODED);

        verify();
    }

    @Test
    public void to_string_same_as_to_uri()
    {
        RequestPathOptimizer optimizer = mockRequestPathOptimizer();
        Response response = mockResponse();

        train_optimizePath(optimizer, "/bar/" + RAW_PATH, OPTIMIZED);
        train_encodeURL(response, OPTIMIZED, ENCODED);

        replay();

        Link link = new LinkImpl("/bar/" + RAW_PATH, true, false, response, optimizer);

        assertEquals(link.toString(), ENCODED);

        verify();
    }

    @Test
    public void retrieve_parameter_values()
    {
        RequestPathOptimizer optimizer = mockRequestPathOptimizer();
        Response response = mockResponse();

        replay();

        Link link = new LinkImpl("/foo/bar", true, false, response, optimizer);

        link.addParameter("fred", "flintstone");

        assertEquals(link.getParameterValue("fred"), "flintstone");

        verify();
    }

    @Test
    public void url_with_anchor()
    {
        RequestPathOptimizer optimizer = mockRequestPathOptimizer();
        Response response = mockResponse();

        train_optimizePath(optimizer, "/foo/bar", OPTIMIZED);
        train_encodeURL(response, OPTIMIZED, ENCODED);

        replay();

        Link link = new LinkImpl("/foo/bar", true, false, response, optimizer);
        link.setAnchor("wilma");

        assertSame(link.getAnchor(), "wilma");

        assertEquals(link.toURI(), ENCODED + "#" + "wilma");

        verify();
    }


    @Test
    public void force_absolute_uri()
    {
        RequestPathOptimizer optimizer = mockRequestPathOptimizer();
        Response response = mockResponse();

        train_encodeURL(response, "/ctx/foo", ENCODED);

        replay();


        Link link = new LinkImpl("/ctx/foo", true, false, response, optimizer);

        assertEquals(link.toAbsoluteURI(), ENCODED);


        verify();
    }
}
