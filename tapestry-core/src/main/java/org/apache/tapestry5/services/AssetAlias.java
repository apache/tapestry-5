// Copyright 2013-2014 The Apache Software Foundation
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http:#www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.apache.tapestry5.services;

/**
 * Identifies a virtual folder and a path within that folder.
 *
 * @since 5.4
 * @deprecated Deprecated in 5.4 (see notes in {@link ClasspathAssetAliasManager}).
 */
public class AssetAlias
{
    public final String virtualFolder, path;

    public AssetAlias(String virtualFolder, String path)
    {
        this.virtualFolder = virtualFolder;
        this.path = path;
    }
}
