// Copyright 2009 The Apache Software Foundation
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
import org.apache.tapestry5.internal.EmptyEventContext;
import org.apache.tapestry5.internal.test.InternalBaseTestCase;
import org.apache.tapestry5.ioc.services.TypeCoercer;
import org.apache.tapestry5.services.*;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Most of the testing is implemented through legacy tests against code that uses CELE.
 *
 * @since 5.1.0.1
 */
public class ComponentEventLinkEncoderImplTest extends InternalBaseTestCase
{
    private TypeCoercer typeCoercer;

    @BeforeClass
    public void setup()
    {
        typeCoercer = getService(TypeCoercer.class);
    }

    @Test
    public void locale_not_encoded()
    {
        RequestSecurityManager manager = mockRequestSecurityManager();
        Request request = mockRequest();
        Response response = mockResponse();
        RequestPathOptimizer optimizer = mockRequestPathOptimizer();
        ContextPathEncoder contextPathEncoder = getService(ContextPathEncoder.class);

        expect(manager.getBaseURL("MyPage")).andReturn(null);
        train_getContextPath(request, "/myapp");

        train_encodeURL(response, "/myapp/mypage", "MAGIC");

        replay();

        ComponentEventLinkEncoder encoder = new ComponentEventLinkEncoderImpl(null, contextPathEncoder, null, request,
                                                                              response,
                                                                              manager, optimizer, null,
                                                                              false);

        PageRenderRequestParameters parameters = new PageRenderRequestParameters("MyPage", new EmptyEventContext());

        Link link = encoder.createPageRenderLink(parameters);

        assertEquals(link.toAbsoluteURI(), "MAGIC");

        verify();
    }

    @Test
    public void index_stripped_off()
    {
        RequestSecurityManager manager = mockRequestSecurityManager();
        Request request = mockRequest();
        Response response = mockResponse();
        RequestPathOptimizer optimizer = mockRequestPathOptimizer();
        ContextPathEncoder contextPathEncoder = getService(ContextPathEncoder.class);

        expect(manager.getBaseURL("admin/Index")).andReturn(null);
        train_getContextPath(request, "");

        train_encodeURL(response, "/admin/abc", "MAGIC");

        replay();

        ComponentEventLinkEncoder encoder = new ComponentEventLinkEncoderImpl(null, contextPathEncoder, null, request,
                                                                              response,
                                                                              manager, optimizer, null,
                                                                              false);

        PageRenderRequestParameters parameters = new PageRenderRequestParameters("admin/Index",
                                                                                 new ArrayEventContext(typeCoercer,
                                                                                                       "abc"));

        Link link = encoder.createPageRenderLink(parameters);

        assertEquals(link.toAbsoluteURI(), "MAGIC");

        verify();
    }

    @Test
    public void root_index_page_gone()
    {
        RequestSecurityManager manager = mockRequestSecurityManager();
        Request request = mockRequest();
        Response response = mockResponse();
        RequestPathOptimizer optimizer = mockRequestPathOptimizer();
        ContextPathEncoder contextPathEncoder = getService(ContextPathEncoder.class);

        expect(manager.getBaseURL("Index")).andReturn(null);
        train_getContextPath(request, "");

        train_encodeURL(response, "/", "MAGIC");

        replay();

        ComponentEventLinkEncoder encoder = new ComponentEventLinkEncoderImpl(null, contextPathEncoder, null, request,
                                                                              response,
                                                                              manager, optimizer, null,
                                                                              false);

        PageRenderRequestParameters parameters = new PageRenderRequestParameters("Index", new EmptyEventContext());

        Link link = encoder.createPageRenderLink(parameters);

        assertEquals(link.toAbsoluteURI(), "MAGIC");

        verify();

    }
}
