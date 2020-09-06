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

import org.apache.tapestry5.http.Link;
import org.apache.tapestry5.http.services.Request;
import org.apache.tapestry5.internal.services.PageRenderDispatcher;
import org.apache.tapestry5.ioc.annotations.UsesOrderedConfiguration;
import org.apache.tapestry5.services.ComponentEventLinkEncoder;
import org.apache.tapestry5.services.LocalizationSetter;
import org.apache.tapestry5.services.PageRenderRequestParameters;

/**
 * Allows the default {@link Link} for a page render request to be replaced.
 * This is a service, but also the contribution to the service, as a chain of command.
 *
 * A contributed implementation of this interface is expected to identify which requests it wants to transform. The
 * {@link #transformPageRenderLink(Link, PageRenderRequestParameters)} method can return a {@link Link} that is allowed
 * to differ from Tapestry normal default. Later, when that request is triggered,
 * {@link #decodePageRenderRequest(Request)} is required to reverse the operation, identifying the original parameters
 * so that request handling can continue.
 * 
 * @since 5.2.0
 */
@UsesOrderedConfiguration(PageRenderLinkTransformer.class)
public interface PageRenderLinkTransformer
{
    /**
     * Transforms a page render link.
     * 
     * @param defaultLink
     *            default Link for this request
     * @param parameters
     *            that define the request
     * @return replacement Link, or null
     */
    Link transformPageRenderLink(Link defaultLink, PageRenderRequestParameters parameters);

    /**
     * Attempts to decode the page render request, to perform the opposite action for
     * {@link #transformPageRenderLink(Link, PageRenderRequestParameters)}. The transformer
     * is also responsible for identifying the locale in the request (as part of the path, or as a
     * query parameter or cookie) and setting the locale for the request.
     *
     * This method will be invoked from the {@link PageRenderDispatcher} and a non-null value returned from this method
     * will prevent the default {@link ComponentEventLinkEncoder#decodePageRenderRequest(Request)} method from being
     * invoked.
     * 
     * @return decoded parameters, or null to proceed normally
     * @see LocalizationSetter#setLocaleFromLocaleName(String)
     */
    PageRenderRequestParameters decodePageRenderRequest(Request request);
}
