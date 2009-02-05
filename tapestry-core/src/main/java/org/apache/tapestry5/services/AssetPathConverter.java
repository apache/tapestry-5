// Copyright 2009 The Apache Software Foundation
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

/**
 * Converts the {@linkplain org.apache.tapestry5.Asset#toClientURL() path (or URI) of an asset} into a new format. This
 * is the <em>hook</em> needed to make use of a <a href="http://en.wikipedia.org/wiki/Content_Delivery_Network">Content
 * Delivery Network</a>.
 * <p/>
 * The default implementation of this is <em>identity</em>, the URI is passed through unchanged. Using a contribution to
 * the {@link org.apache.tapestry5.ioc.services.ServiceOverride} service, you may override the default implementation.
 *
 * @since 5.1.0.0
 */
public interface AssetPathConverter
{
    /**
     * Converts the default asset client URI to its final form, ready to be sent to the client. The default asset path
     * is an absolute path (it starts with a leading slash) and incorporates the context path if any.
     *
     * @param assetPath default asset path
     * @return a URI that can be sent to the client
     */
    String convertAssetPath(String assetPath);
}
