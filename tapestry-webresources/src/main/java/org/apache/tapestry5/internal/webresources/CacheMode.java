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

package org.apache.tapestry5.internal.webresources;

/**
 * Controls caching for {@link ResourceTransformerFactory} in <em>development mode</em>. In production mode, caching at this
 * level is not needed, because artifacts are also cached later in the pipeline. This caching is all about avoid unwanted
 */
public enum CacheMode
{
    /**
     * Cache the content on the file system, in the directory defined by {@link org.apache.tapestry5.webresources.WebResourcesSymbols#CACHE_DIR}.
     * This allows compilation to be avoided even after a restart, as long as the source file has not changed. This only works
     * for compilations that operate on a single file (such as CoffeeScript, but not Less, which has an {@code @import} statement).
     */
    SINGLE_FILE,

    /**
     * The source may be multiple files (e.g., Less). Cache in memory, and invalidate the cache if any of the multiple
     * file's content changes.
     */
    MULTIPLE_FILE,

    /**
     * Do no caching. This is appropriate for extremely cheap compilers.
     */
    NONE;
}
