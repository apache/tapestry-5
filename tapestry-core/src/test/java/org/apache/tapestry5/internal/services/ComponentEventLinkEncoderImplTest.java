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
import org.apache.tapestry5.services.ComponentEventLinkEncoder;
import org.apache.tapestry5.services.PageRenderRequestParameters;
import org.apache.tapestry5.services.Request;
import org.apache.tapestry5.services.Response;
import org.testng.annotations.Test;

/**
 * Most of the testing is implemented through legacy tests against code that uses CELE.
 *
 * @since 5.1.0.1
 */
public class ComponentEventLinkEncoderImplTest extends InternalBaseTestCase
{
    @Test
    public void locale_not_encoded()
    {
        RequestSecurityManager manager = mockRequestSecurityManager();
        Request request = mockRequest();
        Response response = mockResponse();
        RequestPathOptimizer optimizer = mockRequestPathOptimizer();

        expect(manager.getBaseURL("MyPage")).andReturn(null);
        train_getContextPath(request, "/myapp");

        train_encodeURL(response, "/myapp/mypage", "MAGIC");

        replay();

        ComponentEventLinkEncoder encoder = new ComponentEventLinkEncoderImpl(null, null, null, request, response,
                                                                              manager, optimizer, null, null, null,
                                                                              false);

        PageRenderRequestParameters parameters = new PageRenderRequestParameters("MyPage", new EmptyEventContext());

        Link link = encoder.createPageRenderLink(parameters);

        assertEquals(link.toAbsoluteURI(), "MAGIC");

        verify();
    }
}
