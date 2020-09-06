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

package org.apache.tapestry5.services.assets;

import org.apache.tapestry5.commons.Resource;
import org.apache.tapestry5.http.ContentType;
import org.apache.tapestry5.ioc.annotations.IncompatibleChange;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * An object, derived from a {@link Resource}, that can be streamed (ultimately, to a client web browser). In addition,
 * a StreamableResource may be transformed (by the stack of interceptors around {@link StreamableResourceSource}: this
 * includes transforming a Resource (example: CoffeeScript to JavaScript compilation), as well as aggregation
 * and compression.
 *
 * @since 5.3
 */
public interface StreamableResource
{
    /**
     * Describes the underlying {@link Resource} (or resources} for this streamable resource; expressly used
     * as part of the object's {@code toString()}.
     */
    String getDescription();

    /**
     * Indicates if the content is compressed, or compressable.
     */
    CompressionStatus getCompression();

    /**
     * Returns the resource's content type.
     */
    @IncompatibleChange(release = "5.4", details = "Changed from type String to ContentType")
    ContentType getContentType();

    /**
     * The size, in bytes, of the underlying bytestream.
     */
    int getSize();

    /**
     * Streams the resource's content to the provided stream. The caller is responsible for flushing or closing
     * the output stream.
     */
    void streamTo(OutputStream os) throws IOException;

    /**
     * Opens the content of the resource as an input stream; the caller is responsible for closing the stream
     * after reading it.
     *
     * @return stream of the contents of the resource
     * @throws IOException
     */
    InputStream openStream() throws IOException;

    /**
     * Returns the time the resource was last modified, with accuracy to one second (so as to match
     * the HTTP request/response date headers).
     */
    long getLastModified();

    /**
     * Compute and return the checksum of the content for this asset; the checksum should be computed
     * based on the uncompressed content.
     *
     * @return checksum for uncompressed content
     * @see AssetChecksumGenerator#generateChecksum(StreamableResource)
     * @since 5.4
     */
    String getChecksum() throws IOException;


    /**
     * Returns a new StreamableResource that includes the provided customizer. Customizers are invoked
     * in the order they are added.
     *
     * @since 5.4
     */
    StreamableResource addResponseCustomizer(ResponseCustomizer customizer);

    /**
     * Returns the customizer, if any, for this resource.  This may represent an aggregate customizer.
     *
     * @since 5.4
     */
    ResponseCustomizer getResponseCustomizer();

    /**
     * Returns a new StreamableResource instance with the new content type.
     *
     * @param newContentType
     * @since 5.4
     */
    StreamableResource withContentType(ContentType newContentType);
}
