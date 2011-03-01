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

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

import org.apache.tapestry5.ioc.Resource;
import org.apache.tapestry5.services.javascript.JavaScriptStack;

/**
 * Defines additional features desired when accessing the content of a {@link Resource} as
 * a {@link StreamableResource}.
 * 
 * @since 5.3.0
 * @see StreamableResourceSource#getStreamableResource(Resource, Set)
 */
public enum StreamableResourceFeature
{
    /**
     * The content may be GZIP compressed (if its content type is {@linkplain CompressionAnalyzer compressable}).
     */
    GZIP_COMPRESSION,

    /**
     * The content may be cached. This is generally desired, except when the content is being accessed so that
     * it can be aggregated with other content (a {@link JavaScriptStack} is the canonical example) and the individual
     * resources are not accessed except when aggregated. There are two layers of caching: for uncompressed content, and
     * for compressed content (where the content is compressable).
     */
    CACHING,

    /**
     * Applies to certain content types (specifically, JavaScript and CSS) where the content can be reduced in size
     * without changing its effective content (i.e., remove unnecessary whitespace, comments, simplify names, etc.).
     */
    MINIMIZATION;

    /**
     * Unmodifiable set of all features.
     */
    public static final Set<StreamableResourceFeature> ALL = Collections.unmodifiableSet(EnumSet
            .allOf(StreamableResourceFeature.class));

    /**
     * Unmodifiable set of all features, excluding {@link #GZIP_COMPRESSION}.
     */
    public static final Set<StreamableResourceFeature> NO_COMPRESSION = Collections.unmodifiableSet(EnumSet.range(
            CACHING, MINIMIZATION));

    /** Unmodifiable and empty. */
    public static final Set<StreamableResourceFeature> NONE = Collections.unmodifiableSet(EnumSet
            .noneOf(StreamableResourceFeature.class));
}
