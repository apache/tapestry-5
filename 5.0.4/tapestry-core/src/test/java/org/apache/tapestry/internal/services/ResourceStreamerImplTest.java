// Copyright 2007 The Apache Software Foundation
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

import static org.easymock.EasyMock.anyInt;
import static org.easymock.EasyMock.anyLong;
import static org.easymock.EasyMock.eq;

import java.io.IOException;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.apache.tapestry.internal.test.InternalBaseTestCase;
import org.apache.tapestry.ioc.Resource;
import org.apache.tapestry.ioc.internal.util.ClasspathResource;
import org.apache.tapestry.services.Request;
import org.apache.tapestry.services.RequestGlobals;
import org.apache.tapestry.services.Response;
import org.testng.annotations.Test;

/**
 * Tests for the {@link ResourceStreamerImpl} class.
 */
public class ResourceStreamerImplTest extends InternalBaseTestCase
{
    @Test
    public void content_type_css() throws IOException
    {
        content_type("text/css", "test.css");
    }

    @Test
    public void content_type_js() throws IOException
    {
        content_type("text/javascript", "test.js");
    }

    @Test
    public void content_type_gif() throws IOException
    {
        content_type("image/gif", "test.gif");
    }

    private void content_type(String contentType, String fileName) throws IOException
    {
        Request request = mockRequest();
        HttpServletResponse hsr = mockHttpServletResponse();

        train_setContentLength(hsr, anyInt());
        train_setDateHeader(hsr, eq("Last-Modified"), anyLong());
        train_setDateHeader(hsr, eq("Expires"), anyLong());
        train_setContentType(hsr, contentType);
        train_getOutputStream(hsr, new TestServletOutputStream());

        replay();

        Response response = new ResponseImpl(hsr);
        ResourceStreamer streamer = getService(ResourceStreamer.class);
        RequestGlobals globals = getService(RequestGlobals.class);

        globals.store(request, response);

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
