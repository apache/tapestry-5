// Copyright 2006, 2007, 2008 The Apache Software Foundation
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

package org.apache.tapestry5.services;

import org.apache.tapestry5.Asset;
import org.apache.tapestry5.ioc.Resource;

/**
 * Used by {@link AssetSource} to create new {@link Asset}s as needed.
 *
 * @see org.apache.tapestry5.services.AssetSource
 */
public interface AssetFactory
{
    /**
     * Returns the Resource representing the root folder of the domain this factory is responsible for.
     */
    Resource getRootResource();

    /**
     * Creates an instance of an asset. Starting with 5.1.0.0, it is preferred (but not required) that the factory
     * return an instance of {@link org.apache.tapestry5.Asset2}.
     *
     * @param resource a resource within this factories domain (derived from the {@linkplain #getRootResource() root
     *                 resource})
     * @return an Asset for the resource
     */
    Asset createAsset(Resource resource);
}
