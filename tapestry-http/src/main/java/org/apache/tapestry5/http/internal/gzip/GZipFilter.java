// Copyright 2009, 2012 The Apache Software Foundation
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

package org.apache.tapestry5.http.internal.gzip;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.tapestry5.http.TapestryHttpSymbolConstants;
import org.apache.tapestry5.http.services.CompressionAnalyzer;
import org.apache.tapestry5.http.services.HttpServletRequestFilter;
import org.apache.tapestry5.http.services.HttpServletRequestHandler;
import org.apache.tapestry5.http.services.ResponseCompressionAnalyzer;
import org.apache.tapestry5.ioc.annotations.Symbol;

/**
 * Filter that adds GZIP compression to the response, if the client supports it.
 */
public class GZipFilter implements HttpServletRequestFilter
{
    private final int cutover;

    private final ResponseCompressionAnalyzer responseAnalyzer;

    private final CompressionAnalyzer compressionAnalyzer;

    public GZipFilter(
            @Symbol(TapestryHttpSymbolConstants.MIN_GZIP_SIZE)
            int cutover,

            ResponseCompressionAnalyzer responseAnalyzer,

            CompressionAnalyzer compressionAnalyzer)
    {
        this.cutover = cutover;
        this.responseAnalyzer = responseAnalyzer;
        this.compressionAnalyzer = compressionAnalyzer;
    }

    public boolean service(HttpServletRequest request, HttpServletResponse response, HttpServletRequestHandler handler)
            throws IOException
    {
        HttpServletResponse newResponse = responseAnalyzer.isGZipSupported()
                ? new GZIPEnabledResponse(response, request, cutover, compressionAnalyzer)
                : response;

        return handler.service(request, newResponse);
    }
}
