// Copyright 2006, 2008, 2011, 2013 The Apache Software Foundation
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

package org.apache.tapestry5.internal.services;

import org.apache.tapestry5.commons.Resource;
import org.apache.tapestry5.services.assets.StreamableResource;
import org.apache.tapestry5.services.assets.StreamableResourceSource;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.EnumSet;
import java.util.Set;

/**
 * Responsible for streaming the contents of a resource to the client. This is sometimes a simple
 * {@link Resource} (often from the {@link org.apache.tapestry5.internal.services.javascript.ModuleDispatcher},
 * or more frequently an asset represented as a {@link StreamableResource} (via {@link AssetDispatcher}, {@link org.apache.tapestry5.services.assets.AssetRequestHandler},
 * and {@link StreamableResourceSource}). As of 5.4, the ResourceStreamer handles ETag support, as well as
 * validation of the checksum (provided in the URL).
 *
 * @since 5.1.0.0
 */
public interface ResourceStreamer
{
    /**
     * Used to customize the behavior of {@link #streamResource(org.apache.tapestry5.commons.Resource, java.lang.String, java.util.Set)}.
     *
     * @since 5.4
     */
    enum Options
    {
        /**
         * Omit the normal expiration date header; this is appropriate for JavaScript modules, which cannot include
         * their own checksum in the URL (instead, we rely on ETags to prevent unwanted data transfer).
         */
        OMIT_EXPIRATION
    }

    static final Set<Options> DEFAULT_OPTIONS = EnumSet.noneOf(Options.class);

    /**
     * Streams the content of the resource to the client (or sends
     * an alternative response such as {@link HttpServletResponse#SC_NOT_MODIFIED}). Encapsulates logic for compression
     * and for caching.
     *
     * @param resource
     *         to stream
     * @param providedChecksum
     *         checksum from URL (or null to not validate against checksum, which is normal for modules)
     * @param options
     *         enable or disable certain features
     * @see StreamableResourceSource
     */
    boolean streamResource(Resource resource, String providedChecksum, Set<Options> options) throws IOException;

    /**
     * Streams a resource that has been assembled elsewhere.  The StreamableResource may reflect either a normal
     * or a compressed stream, depending on the type of resource and the capabilities of the client.
     *
     * @param resource
     *         content to stream
     * @param providedChecksum
     *         checksum provided (in the URL) to validate against the {@linkplain org.apache.tapestry5.services.assets.StreamableResource#getChecksum()} actual checksum}
     *         for the resource, may be blank to not validate against the checksum
     * @param options
     *         enable or disable certain features
     * @return true if the request was handled (even if sending a {@link HttpServletResponse#SC_NOT_MODIFIED} response),
     *         or false if the request was not handled (because the provided checksum did not match the actual checksum).
     * @throws IOException
     * @since 5.4
     */
    boolean streamResource(StreamableResource resource, String providedChecksum, Set<Options> options) throws IOException;
}
