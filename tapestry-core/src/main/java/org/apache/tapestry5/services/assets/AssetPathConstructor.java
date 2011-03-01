// Copyright 2010, 2011 The Apache Software Foundation
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

/**
 * Encapsulates the logic or creating the path portion of an asset URL, including
 * the application version.
 * 
 * @since 5.2.0
 */
public interface AssetPathConstructor
{
    /**
     * Constructs an asset URL path from the virtual folder and path (within the virtual folder).
     * 
     * @param virtualFolder
     *            corresponds to a {@link AssetRequestHandler} contributed to the AssetDispatcher service
     * @param path
     *            within the virtual folder (should <em>not</em> start with a slash)
     * @return path portion of asset URL, including the context path, the /assets/ virtual folder, the application
     *         version number,
     *         the virtual folder, and the path extension
     */
    String constructAssetPath(String virtualFolder, String path);
}
