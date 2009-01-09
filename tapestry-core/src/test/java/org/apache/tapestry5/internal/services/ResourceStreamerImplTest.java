// Copyright 2007, 2008, 2009 The Apache Software Foundation
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

import org.apache.tapestry5.internal.InternalConstants;
import org.apache.tapestry5.internal.test.InternalBaseTestCase;
import org.apache.tapestry5.ioc.Resource;
import org.apache.tapestry5.ioc.internal.util.ClasspathResource;
import org.apache.tapestry5.services.*;
import static org.easymock.EasyMock.*;
import org.testng.annotations.Test;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Tests for the {@link ResourceStreamerImpl} class.
 */
public class ResourceStreamerImplTest extends InternalBaseTestCase
{
    @Test
    public void content_type_css() throws IOException
    {
        content_type("text/css", "test.css", true);
    }

    @Test
    public void content_type_js() throws IOException
    {
        content_type("text/javascript", "test.js", true);
    }

    @Test
    public void content_type_gif() throws IOException
    {
        content_type("image/gif", "test.gif", false);
    }

    private void content_type(String contentType, String fileName, boolean consultsContext) throws IOException
    {
        Request request = mockRequest();
        HttpServletRequest hsRequest = mockHttpServletRequest();
        HttpServletResponse hsResponse = mockHttpServletResponse();
        Context context = mockContext();

        request.setAttribute(InternalConstants.SUPPRESS_COMPRESSION, true);

        // We're testing with a fake client that does not support GZIP compression:

        expect(hsRequest.getHeader(isA(String.class))).andReturn(null);

        train_setContentLength(hsResponse, anyInt());
        train_setDateHeader(hsResponse, eq("Last-Modified"), anyLong());
        train_setDateHeader(hsResponse, eq("Expires"), anyLong());
        train_setContentType(hsResponse, contentType);
        train_getOutputStream(hsResponse, new TestServletOutputStream());

        if (consultsContext)
            expect(context.getMimeType(endsWith(fileName))).andReturn(null);


        replay();

        Response response = new ResponseImpl(hsResponse);
        ResourceStreamer streamer = getService(ResourceStreamer.class);
        RequestGlobals globals = getService(RequestGlobals.class);

        globals.storeServletRequestResponse(hsRequest, hsResponse);
        globals.storeRequestResponse(request, response);

        getService(ApplicationGlobals.class).storeContext(context);

        String path = getClass().getPackage().getName().replace('.', '/') + "/" + fileName;

        Resource resource = new ClasspathResource(path);

        streamer.streamResource(resource);

        verify();
    }

    private static class TestServletOutputStream extends ServletOutputStream
    {
        @Override
        public void write(int b) throws IOException
        {
            // Empty.
        }
    }
}
