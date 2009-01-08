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

package org.apache.tapestry5.internal.gzip;

import org.apache.tapestry5.internal.InternalConstants;
import org.apache.tapestry5.services.ResponseCompressionAnalyzer;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.IOException;

public class GZIPEnabledResponse extends HttpServletResponseWrapper
{
    private final int cutover;

    private final HttpServletRequest request;

    private final HttpServletResponse response;

    private final ResponseCompressionAnalyzer analyzer;

    public GZIPEnabledResponse(HttpServletResponse response, HttpServletRequest request, int cutover,
                               ResponseCompressionAnalyzer analyzer)
    {
        super(response);

        this.request = request;
        this.response = response;
        this.cutover = cutover;
        this.analyzer = analyzer;
    }

    @Override
    public ServletOutputStream getOutputStream() throws IOException
    {
        if (request.getAttribute(InternalConstants.SUPPRESS_COMPRESSION) != null)
            return super.getOutputStream();

        String contentType = getContentType();

        return new BufferedGZipOutputStream(contentType, response, cutover, analyzer);
    }
}
