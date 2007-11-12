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

package org.apache.tapestry.upload.services;

import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.tapestry.services.HttpServletRequestFilter;
import org.apache.tapestry.services.HttpServletRequestHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Filter that decodes an incoming multipart request.
 */
public class MultipartServletRequestFilter implements HttpServletRequestFilter
{
    private final MultipartDecoder _decoder;

    public MultipartServletRequestFilter(MultipartDecoder multipartDecoder)
    {
        _decoder = multipartDecoder;
    }

    public boolean service(HttpServletRequest request, HttpServletResponse response,
                           HttpServletRequestHandler handler) throws IOException
    {
        if (ServletFileUpload.isMultipartContent(request)) request = _decoder.decode(request);

        return handler.service(request, response);
    }

}
