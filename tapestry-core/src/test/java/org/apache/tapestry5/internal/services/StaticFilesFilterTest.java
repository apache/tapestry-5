// Copyright 2007, 2008 The Apache Software Foundation
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

import org.apache.tapestry5.internal.test.InternalBaseTestCase;
import org.apache.tapestry5.services.*;
import org.testng.annotations.Test;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URL;

public class StaticFilesFilterTest extends InternalBaseTestCase
{
    @Test
    public void request_for_favicon() throws IOException
    {
        Request request = newRequest("/favicon.ico");
        Response response = mockResponse();
        RequestHandler handler = mockRequestHandler();
        Context context = mockContext();

        replay();

        RequestFilter filter = new StaticFilesFilter(context);

        assertFalse(filter.service(request, response, handler));

        verify();
    }

    @Test
    public void path_does_not_contain_a_period() throws Exception
    {
        Request request = newRequest("/start");
        Response response = mockResponse();
        RequestHandler handler = mockRequestHandler();
        Context context = mockContext();

        train_service(handler, request, response, true);

        replay();

        RequestFilter filter = new StaticFilesFilter(context);

        assertTrue(filter.service(request, response, handler));

        verify();
    }

    @Test
    public void existing_file() throws Exception
    {
        URL url = new URL("file://.");
        String path = "/cell.gif";

        Request request = newRequest(path);
        Response response = mockResponse();
        RequestHandler handler = mockRequestHandler();
        Context context = mockContext();

        train_getResource(context, path, url);

        replay();

        RequestFilter filter = new StaticFilesFilter(context);

        assertFalse(filter.service(request, response, handler));

        verify();
    }

    @Test
    public void existing_template_file() throws Exception
    {
        URL url = new URL("file://.");
        String path = "/cell.tml";

        Request request = newRequest(path);
        Response response = mockResponse();
        RequestHandler handler = mockRequestHandler();
        Context context = mockContext();

        train_getResource(context, path, url);

        response.sendError(
                HttpServletResponse.SC_FORBIDDEN,
                "URI /cell.tml may not be accessed remotely.");

        replay();

        RequestFilter filter = new StaticFilesFilter(context);

        assertTrue(filter.service(request, response, handler));

        verify();
    }

    @Test
    public void existing_template_file_case_insenitive() throws Exception
    {
        URL url = new URL("file://.");
        String path = "/cell.TML";

        Request request = newRequest(path);
        Response response = mockResponse();
        RequestHandler handler = mockRequestHandler();
        Context context = mockContext();

        train_getResource(context, path, url);

        response.sendError(
                HttpServletResponse.SC_FORBIDDEN,
                "URI /cell.TML may not be accessed remotely.");

        replay();

        RequestFilter filter = new StaticFilesFilter(context);

        assertTrue(filter.service(request, response, handler));

        verify();
    }

    @Test
    public void not_a_static_file_request() throws Exception
    {
        String path = "/start.update";

        Request request = newRequest(path);
        Response response = mockResponse();
        RequestHandler handler = mockRequestHandler();
        Context context = mockContext();

        train_getResource(context, path, null);
        train_service(handler, request, response, true);

        replay();

        RequestFilter filter = new StaticFilesFilter(context);

        assertTrue(filter.service(request, response, handler));

        verify();
    }

    /**
     * TAPESTRY-2606
     */
    @Test
    public void colon_in_path_prevents_static_file_check() throws Exception
    {
        String path = "/start.update:anevent";

        Request request = newRequest(path);
        Response response = mockResponse();
        RequestHandler handler = mockRequestHandler();
        Context context = mockContext();

        train_service(handler, request, response, true);

        replay();

        RequestFilter filter = new StaticFilesFilter(context);

        assertTrue(filter.service(request, response, handler));

        verify();
    }

    protected final void train_getResource(Context context, String path, URL url)
    {
        expect(context.getResource(path)).andReturn(url);
    }

    protected final Request newRequest(String path)
    {
        Request request = mockRequest();

        train_getPath(request, path);

        return request;
    }

}
