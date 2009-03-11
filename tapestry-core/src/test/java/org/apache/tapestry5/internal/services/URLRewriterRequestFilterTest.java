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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.tapestry5.internal.services.URLRewriterRequestFilter;
import org.apache.tapestry5.ioc.test.TestBase;
import org.apache.tapestry5.services.DelegatingRequest;
import org.apache.tapestry5.services.Request;
import org.apache.tapestry5.services.RequestHandler;
import org.apache.tapestry5.services.Response;
import org.apache.tapestry5.urlrewriter.SimpleRequestWrapper;
import org.apache.tapestry5.urlrewriter.URLRewriterRule;
import org.testng.annotations.Test;

/**
 * Tests {@linkplain org.org.apache.tapestry5.internal.services.URLRewriterRequestFilter}.
 */
public class URLRewriterRequestFilterTest extends TestBase
{

    private final class InternalURLRewriterRule implements URLRewriterRule
    {

        private DelegatingRequest delegetingRequest = new DelegatingRequest();

        public Request process(Request request)
        {
            final String serverName = request.getServerName().replace("JSF", "tapestry");
            final String path = request.getPath().replace(".JSF", "");
            Request wrapper = new SimpleRequestWrapper(request, serverName, path);
            delegetingRequest.setRequest(wrapper);
            return delegetingRequest;
        }

        Request getRequest()
        {
            return delegetingRequest;
        }

    }

    @Test
    public void test_rewriter_rule_chaining() throws IOException
    {

        URLRewriterRule rule1 = new URLRewriterRule()
        {
            public Request process(Request request)
            {
                final String serverName = request.getServerName().toUpperCase();
                final String path = request.getPath().toUpperCase();
                return new SimpleRequestWrapper(request, serverName, path);
            }
        };

        InternalURLRewriterRule rule2 = new InternalURLRewriterRule();

        final Response response = newMock(Response.class);
        RequestHandler handler = newMock(RequestHandler.class);

        Request request = newMock(Request.class);
        expect(request.getServerName()).andReturn("jsf.com");
        expect(request.getPath()).andReturn("/why.jsf");

        List<URLRewriterRule> rules = new ArrayList<URLRewriterRule>();
        rules.add(rule1);
        rules.add(rule2);
        URLRewriterRequestFilter filter = new URLRewriterRequestFilter(rules);

        expect(handler.service(rule2.getRequest(), response)).andReturn(false);

        replay();

        filter.service(request, response, handler);

        verify();

        final String serverName = rule2.getRequest().getServerName();
        final String path = rule2.getRequest().getPath();
        
        assertEquals(serverName, "tapestry.COM");
        assertEquals(path, "/WHY");

    }

    @Test
    public void rewriter_rule_returns_null() throws IOException
    {

        URLRewriterRule rule = new URLRewriterRule()
        {
            public Request process(Request request)
            {
                return null;
            }
        };

        List<URLRewriterRule> list = new ArrayList<URLRewriterRule>();
        list.add(rule);

        URLRewriterRequestFilter filter = new URLRewriterRequestFilter(list);
        Request request = newMock(Request.class);
        Response response = newMock(Response.class);
        RequestHandler requestHandler = newMock(RequestHandler.class);

        boolean ok = false;

        try
        {
            filter.service(request, response, requestHandler);
        }
        catch (RuntimeException e)
        {
            ok = true;
        }

        assertTrue(ok);

    }

}
