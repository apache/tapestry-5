// Copyright 2010 The Apache Software Foundation
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

package org.apache.tapestry5.services.linktransform;

import org.apache.tapestry5.Asset;
import org.apache.tapestry5.Link;
import org.apache.tapestry5.ioc.annotations.UsesOrderedConfiguration;

/**
 * Interface that allows the {@link Link} for downloading an Asset to be transformed (or replaced).
 * AssetLinkTransformer is a service but also a contribution to the service, as a chain of command.
 */
@UsesOrderedConfiguration(AssetLinkTransformer.class)
public interface AssetLinkTransformer
{
    /**
     * Transform the Asset link.
     * 
     * @param defaultLink
     *            the normally constructed Link for this asset
     * @param asset
     *            the asset for which the link was constructed (this may be null for virtual assets)
     * @param assetPath
     *            the normal path for the asset (the portion below "/assets" in the
     *            default asset URL)
     * @return a replacement Link to access the Asset, or null
     */
    Link transformAssetLink(Link defaultLink, Asset asset, String assetPath);
}
