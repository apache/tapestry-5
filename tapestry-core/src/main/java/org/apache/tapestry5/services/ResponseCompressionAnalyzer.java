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

package org.apache.tapestry5.services;

import org.apache.tapestry5.ioc.annotations.UsesConfiguration;

/**
 * Used to determine if the client supports GZIP compression of the response.
 * <p/>
 * The configuration is an unordered list of content types that should <em>not</em> be compressed.
 *
 * @since 5.1.0.0
 */
@UsesConfiguration(String.class)
public interface ResponseCompressionAnalyzer
{
    /**
     * Checks the Accept-Encoding request header for a "gzip" token.
     *
     * @return true if gzip is supported by client
     */
    boolean isGZipSupported();

    /**
     * Checks to see if the indicated content type is compressable. Many formats are already compressed; pushing them
     * through a GZip filter consumes cycles and makes them larger.
     * <p/>
     * Contribute content type strings to the service's configuration to mark them as not compressable.
     *
     * @param contentType the mime type of the content, such as "text/html" or "image/jpeg".
     * @return true if compression is worthwile
     */
    boolean isCompressable(String contentType);
}
