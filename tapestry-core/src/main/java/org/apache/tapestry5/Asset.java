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

package org.apache.tapestry5;

import org.apache.tapestry5.commons.Resource;

/**
 * An Asset is any kind of resource that can be exposed to the client web browser. Although quite often an Asset is a
 * resource in a web application's context folder, within Tapestry, Assets may also be resources on the classpath (i.e.,
 * packaged inside JARs).
 *
 * An Asset's toString() will return the URL for the resource (the same value as {@link #toClientURL()}).
 *
 * Release 5.1.0.0 introduced <code>org.apache.tapestry5.Asset2</code>, which extends this interface with an additional
 * method.
 * 
 * Release 5.7.0 merged Asset2 into Asset and Asset2 got removed.
 *
 * @see org.apache.tapestry5.services.AssetPathConverter
 */
public interface Asset
{
    /**
     * Returns a URL that can be passed, unchanged, to the client in order for it to access the resource. The same value
     * is returned from <code>toString()</code>.
     *
     * Tapestry's built-in asset types (context and classpath) always incorporate a checksum as part of the path,
     * and alternate implementations are encouraged to do so as well. In addition, Tapestry ensures that context and
     * classpath assets have a far-future expires header (to ensure aggressive caching by the client).
     * Note that starting in Tapestry 5.4, it is expected that Asset instances recognize
     * when the underlying Resource's content has changed, and update the clientURL to reflect the new content's
     * checksum. This wasn't an issue in earlier releases where the clientURL incorporated a version number.
     *
     * Finally, starting in 5.4, this value will often be <em>variant</em>: the exact URL returned will depend on
     * whether the underlying resource content is compressable, whether the current {@link org.apache.tapestry5.http.services.Request}
     * supports compression.
     *
     * @see org.apache.tapestry5.services.AssetSource
     * @see org.apache.tapestry5.services.AssetPathConverter
     */
    String toClientURL();

    /**
     * Returns the underlying Resource for the Asset.
     */
    Resource getResource();

    /**
     * Returns true if the Asset is invariant (meaning that it returns the same value from {@link Asset#toClientURL()}
     * at all times). Most Assets are invariant. Assets that are used as binding values will be cached more aggressively by Tapestry if they are
     * invariant. This default implementation returns <code>false</code>
     *
     * @return true if invariant
     * @see org.apache.tapestry5.services.AssetPathConverter#isInvariant()
     * @see Binding#isInvariant()
     * @since 5.1.0.0 (in Asset2), 5.7.0 (in Asset).
     */
    default boolean isInvariant() 
    {
        return false;
    }

}
