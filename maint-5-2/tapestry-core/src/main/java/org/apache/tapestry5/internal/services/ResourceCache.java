// Copyright 2006, 2008, 2009 The Apache Software Foundation
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

import org.apache.tapestry5.ioc.Resource;
import org.apache.tapestry5.services.InvalidationEventHub;
import org.apache.tapestry5.services.ResourceDigestGenerator;

/**
 * Caches information about resources on the classpath. In addition, acts as an invalidation hub for any resources for
 * which information is obtained (when any of the resources are changed, invalidation listeners are notified so they can
 * clear their caches).
 */
public interface ResourceCache extends InvalidationEventHub
{
    /**
     * Returns true if the path requires that the client URL for the resource include a digest to validate that the
     * client is authorized to access the resource.
     *
     * @param resource
     * @return true if digest is required for the resource
     * @see ResourceDigestGenerator#requiresDigest(String)
     */
    boolean requiresDigest(Resource resource);

    /**
     * Returns the contents of the resource
     *
     * @param resource
     * @return access to compressed and uncompressed streams
     * @since 5.1.0.0
     */
    StreamableResource getStreamableResource(Resource resource);

    /**
     * Returns the digest for the given path.
     *
     * @param resource
     * @return the digest, or null if the resource does not exist
     */
    String getDigest(Resource resource);

    /**
     * Returns the time modified for the resource.
     *
     * @param resource
     * @return the date time modified for the path, or a negative value if the resource does not exist
     */
    long getTimeModified(Resource resource);
}
