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

package org.apache.tapestry5.http.internal.services;

import javax.servlet.http.HttpServletRequest;

import org.apache.tapestry5.commons.util.CommonsUtils;
import org.apache.tapestry5.http.ContentType;
import org.apache.tapestry5.http.TapestryHttpConstants;
import org.apache.tapestry5.http.TapestryHttpSymbolConstants;
import org.apache.tapestry5.http.services.CompressionAnalyzer;
import org.apache.tapestry5.http.services.ResponseCompressionAnalyzer;
import org.apache.tapestry5.ioc.annotations.Symbol;

public class ResponseCompressionAnalyzerImpl implements ResponseCompressionAnalyzer
{
    private final HttpServletRequest request;

    private final boolean gzipCompressionEnabled;

    private final CompressionAnalyzer compressionAnalyzer;

    public ResponseCompressionAnalyzerImpl(HttpServletRequest request,
                                           @Symbol(TapestryHttpSymbolConstants.GZIP_COMPRESSION_ENABLED)
                                           boolean gzipCompressionEnabled, CompressionAnalyzer compressionAnalyzer)
    {
        this.request = request;
        this.gzipCompressionEnabled = gzipCompressionEnabled;
        this.compressionAnalyzer = compressionAnalyzer;
    }

    public boolean isGZipSupported()
    {
        if (!gzipCompressionEnabled)
        {
            return false;
        }

        // TAP5-1880:
        if (request.getProtocol() == "HTTP/1.0")
        {
            return false;
        }

        // TAP5-2264:
        if (request.getAttribute(TapestryHttpConstants.SUPPRESS_COMPRESSION) != null)
        {
            return false;
        }

        String supportedEncodings = request.getHeader("Accept-Encoding");

        if (supportedEncodings == null)
        {
            return false;
        }

        for (String encoding : CommonsUtils.splitAtCommas(supportedEncodings))
        {
            if (encoding.equalsIgnoreCase("gzip"))
            {
                return true;
            }
        }

        return false;
    }

    public boolean isGZipEnabled(ContentType contentType)
    {
        return isGZipSupported() && compressionAnalyzer.isCompressable(contentType.getMimeType());
    }
}
