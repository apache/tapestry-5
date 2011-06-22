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

package org.apache.tapestry5.upload.internal.services;

import org.apache.tapestry5.services.HttpServletRequestHandler;
import org.apache.tapestry5.test.TapestryTestCase;
import org.apache.tapestry5.upload.services.MultipartDecoder;
import org.testng.annotations.Test;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class MultipartServletRequestFilterTest extends TapestryTestCase
{

    @Test
    public void normalRequestDoesNothing() throws Exception
    {
        MultipartDecoder decoder = newMock(MultipartDecoder.class);
        HttpServletRequest request = mockHttpServletRequest();
        HttpServletResponse response = mockHttpServletResponse();
        HttpServletRequestHandler handler = newMock(HttpServletRequestHandler.class);

        MultipartServletRequestFilter filter = new MultipartServletRequestFilter(decoder);

        expect(request.getMethod()).andReturn("get");

        expect(handler.service(request, response)).andReturn(true);

        replay();

        boolean isHandled = filter.service(request, response, handler);
        assertTrue(isHandled);
        verify();
    }

    @Test
    public void multipartRequestIsDecoded() throws Exception
    {
        MultipartDecoder decoder = newMock(MultipartDecoder.class);
        HttpServletRequest request = mockHttpServletRequest();
        HttpServletRequest decodedRequest = mockHttpServletRequest();
        HttpServletResponse response = mockHttpServletResponse();
        HttpServletRequestHandler handler = newMock(HttpServletRequestHandler.class);

        MultipartServletRequestFilter filter = new MultipartServletRequestFilter(decoder);

        expect(request.getMethod()).andReturn("post");
        expect(request.getContentType()).andReturn("multipart/form");
        expect(decoder.decode(request)).andReturn(decodedRequest);

        expect(handler.service(decodedRequest, response)).andReturn(true);

        replay();

        boolean isHandled = filter.service(request, response, handler);
        assertTrue(isHandled);
        verify();
    }
}
