// Copyright 2006, 2008, 2011 The Apache Software Foundation
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

import org.apache.tapestry5.ioc.Resource;
import org.apache.tapestry5.services.assets.StreamableResource;
import org.apache.tapestry5.services.assets.StreamableResourceSource;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Set;

/**
 * Responsible for streaming the contents of a resource to the client. The {@link org.apache.tapestry5.ioc.Resource} to
 * stream is a {@link org.apache.tapestry5.ioc.internal.util.ClasspathResource} or {@link ContextResource}.
 *
 * @since 5.1.0.0
 */
public interface ResourceStreamer
{
    /**
     * Used to customize the behavior of {@link #streamResource(org.apache.tapestry5.ioc.Resource, java.util.Set)}.
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

    /**
     * Streams the content of the resource to the client (or sends
     * an alternative response such as {@link HttpServletResponse#SC_NOT_MODIFIED}). Encapsulates logic for compression
     * and for caching.
     *
     * @see StreamableResourceSource
     */
    void streamResource(Resource resource) throws IOException;

    /**
     * Streams the content of the resource to the client (or sends
     * an alternative response such as {@link HttpServletResponse#SC_NOT_MODIFIED}). Encapsulates logic for compression
     * and for caching.
     *
     * @see StreamableResourceSource
     */
    void streamResource(Resource resource, Set<Options> options) throws IOException;

    /**
     * Streams a resource that has been assembled elsewhere, using default options of none.
     *
     * @param resource
     * @throws IOException
     * @since 5.3
     */
    void streamResource(StreamableResource resource) throws IOException;

    /**
     * Streams a resource that has been assembled elsewhere.
     *
     * @param resource
     *         content to stream
     * @param options
     *         any special options related to streaming the resource
     * @throws IOException
     * @since 5.4
     */
    void streamResource(StreamableResource resource, Set<Options> options) throws IOException;
}
