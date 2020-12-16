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
import org.apache.tapestry5.commons.Resource;

/**
 * Used by {@link AssetSource} to create new {@link Asset}s as needed.
 *
 * Starting in Tapestry 5.4, the built-in implementations of this interface (for context assets, and for classpath assets)
 * were changed so that when underlying resources changed, the client URLs for Assets are discarded; this is necessitated by two factors:
 * 1) the {@linkplain org.apache.tapestry5.Asset#toClientURL() client URL}
 * for an Asset now includes a checksum based on the content of the underlying resource, so a change to resource content
 * (during development) results in a change to the URL.
 * 2) {@link org.apache.tapestry5.services.javascript.JavaScriptStack} (especially the {@link org.apache.tapestry5.services.javascript.ExtensibleJavaScriptStack} implementation)
 * made no provision for rebuilding the Assets post-construction, and there is no backwards compatible way to
 * introduce this concept (and JavaScriptStacks are something many applications and third-party libraries make use of).
 * So, starting in Tapestry 5.4, the implementations of {@link Asset} should be
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
     * Creates an instance of an asset.
     *
     * @param resource
     *         a resource within this factories domain (derived from the {@linkplain #getRootResource() root
     *         resource})
     * @return an Asset for the resource
     */
    Asset createAsset(Resource resource);
}
