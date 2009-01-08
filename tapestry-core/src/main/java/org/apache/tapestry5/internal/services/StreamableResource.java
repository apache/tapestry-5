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

import java.io.IOException;
import java.io.InputStream;

/**
 * Abstracts around a {@link org.apache.tapestry5.ioc.Resource} to allow access to the resource's content either
 * compressed on uncompressed. The advantage is that, for cmpressed streams, the data is only compressed once, rather
 * than for each request.
 *
 * @since 5.1.0.0
 */
public interface StreamableResource
{
    /**
     * Returns the content type available from the underlying {@link java.net.URLConnection}, which may be null.
     *
     * @return content type, or null
     */
    String getContentType() throws IOException;

    /**
     * Returns the size of the content
     *
     * @param compress if true, return size of compressed content
     * @return size
     * @throws IOException
     */
    int getSize(boolean compress) throws IOException;

    /**
     * Returns the raw input stream (wrapped in a {@link java.io.BufferedInputStream}, or the compressed bytestream.
     *
     * @param compress if true, return compressed version
     * @return stream of raw or compressed bytes
     */
    InputStream getStream(boolean compress) throws IOException;

    /**
     * Returns the time the underlying file was last modified.
     */
    long getLastModified() throws IOException;
}
