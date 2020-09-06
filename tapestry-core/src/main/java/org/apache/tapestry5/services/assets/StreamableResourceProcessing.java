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

import org.apache.tapestry5.commons.Resource;
import org.apache.tapestry5.services.javascript.JavaScriptStack;

/**
 * Defines additional features desired when accessing the content of a {@link Resource} as
 * a {@link StreamableResource}.
 *
 * @see StreamableResourceSource
 * @since 5.3
 */
public enum StreamableResourceProcessing
{
    /**
     * The default behavior when the client supports compression, and an individual (non-aggregated) resources
     * is being accessed. The resource may be minimized and both the compressed and uncompressed versions may be cached.
     */
    COMPRESSION_ENABLED,

    /**
     * As with {@link #COMPRESSION_ENABLED}, but the final compression stage (and compression cache) is skipped. This is
     * appropriate
     * for individual resources where the client does not support compression.
     */
    COMPRESSION_DISABLED,

    /**
     * Turns off all caching and minification of the resource, which is appropriate when the individual resource will be
     * aggregated with other resources to form a virtual composite.
     *
     * @see JavaScriptStack
     */
    FOR_AGGREGATION
}
