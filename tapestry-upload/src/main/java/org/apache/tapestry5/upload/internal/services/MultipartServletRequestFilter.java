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

import org.apache.commons.fileupload2.jakarta.JakartaServletFileUpload;
import org.apache.tapestry5.http.services.HttpServletRequestFilter;
import org.apache.tapestry5.http.services.HttpServletRequestHandler;
import org.apache.tapestry5.upload.services.MultipartDecoder;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Filter that decodes an incoming multipart request.
 */
public class MultipartServletRequestFilter implements HttpServletRequestFilter
{
    private final MultipartDecoder decoder;

    public MultipartServletRequestFilter(MultipartDecoder multipartDecoder)
    {
        decoder = multipartDecoder;
    }

    @Override
    public boolean service(HttpServletRequest request, HttpServletResponse response, HttpServletRequestHandler handler)
            throws IOException
    {
        HttpServletRequest newRequest = JakartaServletFileUpload.isMultipartContent(request) ? decoder.decode(
                request) : request;

        return handler.service(newRequest, response);
    }

}
