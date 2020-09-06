// Copyright 2009, 2010, 2012 The Apache Software Foundation
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.apache.tapestry5.http.internal.gzip;

import java.io.IOException;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import org.apache.tapestry5.http.TapestryHttpConstants;
import org.apache.tapestry5.http.services.CompressionAnalyzer;

public class GZIPEnabledResponse extends HttpServletResponseWrapper
{
    private final int cutover;

    private final HttpServletRequest request;

    private final HttpServletResponse response;

    private final CompressionAnalyzer analyzer;

    private boolean contentLengthSet = false;

    public GZIPEnabledResponse(HttpServletResponse response, HttpServletRequest request, int cutover,
                               CompressionAnalyzer analyzer)
    {
        super(response);

        this.request = request;
        this.response = response;
        this.cutover = cutover;
        this.analyzer = analyzer;
    }

    public void setContentLength(int len)
    {
        super.setContentLength(len);

        contentLengthSet = true;
    }

    @Override
    public ServletOutputStream getOutputStream() throws IOException
    {
        if (contentLengthSet || isCompressionDisabled())
            return super.getOutputStream();

        String contentType = getContentType();

        return new BufferedGZipOutputStream(contentType, response, cutover, analyzer);
    }

    private boolean isCompressionDisabled()
    {
        return request.getAttribute(TapestryHttpConstants.SUPPRESS_COMPRESSION) != null;
    }
}
