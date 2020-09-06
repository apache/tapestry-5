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

import org.apache.tapestry5.ioc.annotations.UsesMappedConfiguration;

/**
 * Identifies which content types are compressable. In general, content types are assumed to be compressable. The
 * configuration of the service identifies exceptions, which are usually image file formats.
 *
 * The configuration maps content types to boolean values (true for compressable).
 *
 * Since 5.4, the contributed values may also be a wild-card such as "image/*" (that is, the subtype
 * may be a '*' to match any content type with the same top-level type).
 *
 * @since 5.3
 */
@UsesMappedConfiguration(boolean.class)
public interface CompressionAnalyzer
{
    /**
     * For a given MIME type, is the content compressable via GZip?
     *
     * @param contentType
     *         MIME content type, possibly included attributes such as encoding type
     * @return true if the content is not "naturally" compressed
     */
    boolean isCompressable(String contentType);
}
