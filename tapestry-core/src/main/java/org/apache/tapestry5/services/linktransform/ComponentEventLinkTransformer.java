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
import org.apache.tapestry5.internal.services.ComponentEventDispatcher;
import org.apache.tapestry5.ioc.annotations.UsesOrderedConfiguration;
import org.apache.tapestry5.services.ComponentEventRequestParameters;
import org.apache.tapestry5.services.LocalizationSetter;

/**
 * Allows for selective replacement of the default {@link Link} used to represent a component event request.
 * This is a service, but also the contribution to the service, as a chain of command.
 *
 * This transformer follows the same pattern as {@link PageRenderLinkTransformer}.
 * 
 * @since 5.2.0
 */
@UsesOrderedConfiguration(ComponentEventLinkTransformer.class)
public interface ComponentEventLinkTransformer
{
    /**
     * Allows the default Link created for the component event request to be replaced.
     * 
     * @param defaultLink
     *            the default Link generated for a component event request
     * @param parameters
     *            used to create the default Link
     * @return a replacement Link, or null
     */
    Link transformComponentEventLink(Link defaultLink, ComponentEventRequestParameters parameters);

    /**
     * Attempts to decode the page render request, to perform the opposite action for
     * {@link #transformComponentEventLink(Link, ComponentEventRequestParameters)}. The transformer
     * is also responsible for identifying the locale in the request (as part of the path, or as a
     * query parameter or cookie) and setting the locale for the request.
     *
     * This method will be invoked from the {@link ComponentEventDispatcher} and a non-null value returned from this
     * method will prevent the default
     * {@link org.apache.tapestry5.services.ComponentEventLinkEncoder#decodeComponentEventRequest(Request)} method
     * from being invoked.
     * 
     * @return decoded parameters, or null to proceed normally
     * @see LocalizationSetter#setLocaleFromLocaleName(String)
     */
    ComponentEventRequestParameters decodeComponentEventRequest(Request request);
}
