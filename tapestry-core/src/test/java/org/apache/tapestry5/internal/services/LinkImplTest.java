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
        ComponentInvocation invocation = mockComponentInvocation();

        train_buildURI(invocation, RAW_PATH);

        train_encodeRedirectURL(response, "/base/context/" + RAW_PATH, ENCODED);

        replay();

        Link link = new LinkImpl(response, optimizer, "/base", "/context", invocation);

        assertEquals(link.toRedirectURI(), ENCODED);

        verify();
    }

    @Test
    public void to_string_same_as_to_uri()
    {
        RequestPathOptimizer optimizer = mockRequestPathOptimizer();
        Response response = mockResponse();
        ComponentInvocation invocation = mockComponentInvocation();

        train_buildURI(invocation, RAW_PATH);

        train_optimizePath(optimizer, "/bar/" + RAW_PATH, OPTIMIZED);
        train_encodeURL(response, OPTIMIZED, ENCODED);

        replay();

        Link link = new LinkImpl(response, optimizer, null, "/bar", invocation);

        assertEquals(link.toString(), ENCODED);

        verify();
    }

    @Test
    public void retrieve_parameter_values()
    {
        RequestPathOptimizer optimizer = mockRequestPathOptimizer();
        Response response = mockResponse();
        ComponentInvocation invocation = mockComponentInvocation();

        train_getParameter(invocation, "fred", "flintstone");

        replay();

        Link link = new LinkImpl(response, optimizer, "", "bar", invocation);

        assertEquals(link.getParameterValue("fred"), "flintstone");

        verify();
    }

    @Test
    public void url_with_anchor()
    {
        try_with_anchor("wilma", "/context/" + RAW_PATH + "#wilma");
    }

    @Test
    public void url_with_null_anchor()
    {
        try_with_anchor(null, "/context/" + RAW_PATH);
    }

    @Test
    public void url_with_empty_anchor()
    {
        try_with_anchor("", "/context/" + RAW_PATH);
    }

    private void try_with_anchor(String anchor, String url)
    {
        RequestPathOptimizer optimizer = mockRequestPathOptimizer();
        Response response = mockResponse();
        ComponentInvocation invocation = mockComponentInvocation();

        train_buildURI(invocation, RAW_PATH);
        train_optimizePath(optimizer, url, OPTIMIZED);
        train_encodeURL(response, OPTIMIZED, ENCODED);

        replay();

        Link link = new LinkImpl(response, optimizer, null, "/context", invocation);

        link.setAnchor(anchor);

        assertSame(link.getAnchor(), anchor);

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
                                 new ComponentInvocationImpl(contextPathEncoder, new OpaqueConstantTarget("foo"), null,
                                                             null,
                                                             false)
        );

        assertEquals(link.toAbsoluteURI(), ENCODED);


        verify();
    }
}
