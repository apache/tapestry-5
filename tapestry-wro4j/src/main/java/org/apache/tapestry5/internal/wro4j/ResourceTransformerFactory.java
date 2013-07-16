// Copyright 2013 The Apache Software Foundation
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

package org.apache.tapestry5.internal.wro4j;

import org.apache.tapestry5.services.assets.ResourceTransformer;

/**
 * Creates ResourceTransformer around a named {@link org.apache.tapestry5.wro4j.services.ResourceProcessor}.
 *
 * @see org.apache.tapestry5.services.assets.StreamableResourceSource
 * @since 5.4
 */
public interface ResourceTransformerFactory
{

    /**
     * Constructs a compiler around a named processor.
     *
     * @param contentType
     *         transformed content type, e.g., "text/javascript"
     * @param processorName
     *         name of processor to do work
     * @param sourceName
     *         for debugging: source name, e.g., "CoffeeScript"
     * @param targetName
     *         for debugging: target name, e.g., "JavaScript"
     * @param cacheMode
     *         Indicates if and how the compiled content should be cached (in development mode only)
     * @return transformer
     * @see org.apache.tapestry5.wro4j.services.ResourceProcessorSource
     */
    ResourceTransformer createCompiler(String contentType, String processorName, String sourceName, String targetName, CacheMode cacheMode);

    /**
     * Constructs a compiler around a another ResourceTransformer implementation. In development mode, the wrapped version
     * will handle caching, as well as logging output of timing for the real implementation.
     *
     * @param sourceName
     *         for debugging: source name, e.g., "Less"
     * @param targetName
     *         for debugging: target name, e.g., "CSS"
     * @param transformer
     *         performs the actual work
     * @param cacheMode
     *         Indicates if and how the compiled content should be cached (in development mode only)
     * @return transformer
     * @see org.apache.tapestry5.wro4j.services.ResourceProcessorSource
     */
    ResourceTransformer createCompiler(String contentType, String sourceName, String targetName, ResourceTransformer transformer, CacheMode cacheMode);

}
