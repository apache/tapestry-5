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

package org.apache.tapestry5.http.services;

import org.apache.tapestry5.http.ContentType;

/**
 * Used to determine if the client supports GZip compression of the response.
 *
 * @see CompressionAnalyzer
 * @see org.apache.tapestry5.http.TapestryHttpSymbolConstants#GZIP_COMPRESSION_ENABLED
 * @since 5.1.0.0
 */
public interface ResponseCompressionAnalyzer
{
    /**
     * Checks the Accept-Encoding request header for a "gzip" token. Ensures that the protocol is not "HTTP/1.0", which
     * does not correctly support GZip encoding (in older Internet Explorer browsers).
     *
     * @return true if gzip is supported by client
     */
    boolean isGZipSupported();

    /**
     * Uses {@link CompressionAnalyzer} to determine if the content is compressable, but only if the request
     * indicates the client supports compression.
     *
     * @param contentType
     * @return true if the content can be compressed for the current request
     * @since 5.4
     */
    boolean isGZipEnabled(ContentType contentType);
}
