// Copyright 2011 The Apache Software Foundation
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

package org.apache.tapestry5.services.assets;

import org.apache.tapestry5.ioc.Resource;
import org.apache.tapestry5.ioc.annotations.UsesMappedConfiguration;

import java.io.IOException;

/**
 * Converts {@link Resource}s into {@link StreamableResource}s, and may be responsible for
 * {@linkplain ResourceTransformer transforming} resources based on file extension. In addition,
 * service decorators added to the service may provide additional processing (compression, minimization, and caching).
 *
 * @since 5.3
 */
@UsesMappedConfiguration(ResourceTransformer.class)
public interface StreamableResourceSource
{
    /**
     * Converts a Resource (which must be non-null and exist) into a streamable resource, along with
     * some additional optional behaviors.
     *
     * @param baseResource the resource to convert
     * @param processing   defines additional processing after the resource has been read and possibly transformed
     * @param dependencies Passed to any {@link ResourceTransformer} to track additional dependencies of the base resource
     * @return the contents of the Resource, possibly transformed, in a streamable format.
     * @throws IOException if the resource does not exist or a URL for the content is not available
     */
    StreamableResource getStreamableResource(Resource baseResource, StreamableResourceProcessing processing, ResourceDependencies dependencies)
            throws IOException;
}
