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

package org.apache.tapestry5.internal.services;

import org.apache.tapestry5.internal.TapestryInternalUtils;
import org.apache.tapestry5.ioc.annotations.Symbol;
import org.apache.tapestry5.ioc.internal.util.CollectionFactory;
import org.apache.tapestry5.services.ResponseCompressionAnalyzer;
import org.apache.tapestry5.SymbolConstants;

import javax.servlet.http.HttpServletRequest;
import java.util.Collection;
import java.util.Map;

public class ResponseCompressionAnalyzerImpl implements ResponseCompressionAnalyzer
{
    private final HttpServletRequest request;

    private final Map<String, Boolean> notCompressable = CollectionFactory.newCaseInsensitiveMap();

    private final boolean gzipCompressionEnabled;

    public ResponseCompressionAnalyzerImpl(HttpServletRequest request, Collection<String> configuration,
                                           @Symbol(SymbolConstants.GZIP_COMPRESSION_ENABLED)
                                           boolean gzipCompressionEnabled)
    {
        this.request = request;
        this.gzipCompressionEnabled = gzipCompressionEnabled;

        for (String contentType : configuration)
            notCompressable.put(contentType, true);
    }

    public boolean isGZipSupported()
    {
        if (!gzipCompressionEnabled) return false;

        String supportedEncodings = request.getHeader("Accept-Encoding");

        if (supportedEncodings == null) return false;

        for (String encoding : TapestryInternalUtils.splitAtCommas(supportedEncodings))
        {
            if (encoding.equalsIgnoreCase("gzip"))
                return true;
        }

        return false;
    }

    public boolean isCompressable(String contentType)
    {
        int x = contentType.indexOf(';');

        String key = x < 0 ? contentType : contentType.substring(0, x);

        return notCompressable.get(key) == null;
    }
}
