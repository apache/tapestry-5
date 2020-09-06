// Copyright 2010, 2011, 2013 The Apache Software Foundation
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

import org.apache.tapestry5.ioc.annotations.IncompatibleChange;

import java.io.IOException;

/**
 * Encapsulates the logic or creating the path portion of an asset URL, including hooking the {@link org.apache.tapestry5.services.AssetPathConverter}
 * into the generation.
 *
 * @see org.apache.tapestry5.services.PathConstructor
 * @since 5.2.0
 */
public interface AssetPathConstructor
{
    /**
     * Constructs an asset URL path from the virtual folder and path (within the virtual folder).
     * After constructing the string (and honoring the {@link org.apache.tapestry5.SymbolConstants#ASSET_URL_FULL_QUALIFIED}
     * symbol), the result is passed through the {@link org.apache.tapestry5.services.AssetPathConverter}.
     *
     * @param virtualFolder
     *         corresponds to a {@link AssetRequestHandler} contributed to the AssetDispatcher service
     * @param path
     *         a path that can be used to identify the underlying {@link org.apache.tapestry5.commons.Resource} or
     *         or re-acquire the {@link StreamableResource}; this will be the final portion of the URL, after
     *         the appropriate prefix (based on whether the resource is compressed or not) and the checksum for the
     *         resource
     * @param resource
     *         underlying resource for the asset path; the checksum portion of the URL is obtained from the resource
     * @return path portion of asset URL, which is everything needed by the {@link org.apache.tapestry5.internal.services.AssetDispatcher}
     *         to find and stream the resource
     * @see StreamableResourceSource
     */
    @IncompatibleChange(release = "5.4", details = "resource parameter added, IOException may now be thrown")
    String constructAssetPath(String virtualFolder, String path, StreamableResource resource) throws IOException;

}
