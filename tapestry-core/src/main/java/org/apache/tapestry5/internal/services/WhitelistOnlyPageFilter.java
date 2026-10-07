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

package org.apache.tapestry5.internal.services;

import org.apache.tapestry5.MetaDataConstants;
import org.apache.tapestry5.http.services.Request;
import org.apache.tapestry5.internal.InternalConstants;
import org.apache.tapestry5.services.ComponentEventRequestParameters;
import org.apache.tapestry5.services.ComponentRequestFilter;
import org.apache.tapestry5.services.ComponentRequestHandler;
import org.apache.tapestry5.services.MetaDataLocator;
import org.apache.tapestry5.services.PageRenderRequestParameters;
import org.apache.tapestry5.services.security.ClientWhitelist;

import java.io.IOException;

/**
 * Ensures that a component event request does not reach a page annotated with
 * {@link org.apache.tapestry5.annotations.WhitelistAccessOnly} from a client that is not on the
 * {@linkplain ClientWhitelist whitelist}.
 *
 * A component event request identifies two pages: the active page (from the URL path) and the
 * containing page (from the {@link InternalConstants#CONTAINER_PAGE_NAME} request parameter), whose
 * component actually handles the event. Code from both pages runs for the request, so both are
 * checked here.
 *
 * {@link ComponentEventLinkEncoderImpl} performs the same check while decoding the request, and is
 * the gate that normally rejects these requests. This filter repeats it independently, so that any
 * other route to the component event pipeline is covered as well.
 *
 * Page render requests are passed through: they are checked while decoding, and the page render
 * dispatcher does not honour the "component not found" attribute used here to force a 404.
 *
 * @see org.apache.tapestry5.annotations.WhitelistAccessOnly
 * @see MetaDataConstants#WHITELIST_ONLY_PAGE
 * @since 5.9.2
 */
public class WhitelistOnlyPageFilter implements ComponentRequestFilter
{
    private final Request request;

    private final MetaDataLocator metaDataLocator;

    private final ClientWhitelist clientWhitelist;

    public WhitelistOnlyPageFilter(Request request, MetaDataLocator metaDataLocator, ClientWhitelist clientWhitelist)
    {
        this.request = request;
        this.metaDataLocator = metaDataLocator;
        this.clientWhitelist = clientWhitelist;
    }

    @Override
    public void handleComponentEvent(ComponentEventRequestParameters parameters, ComponentRequestHandler handler)
            throws IOException
    {
        if (isWhitelistOnlyAndNotValid(parameters.getActivePageName())
                || isWhitelistOnlyAndNotValid(parameters.getContainingPageName()))
        {
            // Same treatment as a request naming a component that does not exist: handling is
            // aborted and the dispatcher turns the request into a 404, rather than reporting that
            // the page exists but is protected.

            request.setAttribute(InternalConstants.REFERENCED_COMPONENT_NOT_FOUND, true);

            return;
        }

        handler.handleComponentEvent(parameters);
    }

    @Override
    public void handlePageRender(PageRenderRequestParameters parameters, ComponentRequestHandler handler)
            throws IOException
    {
        // Pass these through to the default handler.
        handler.handlePageRender(parameters);
    }

    private boolean isWhitelistOnlyAndNotValid(String canonicalizedPageName)
    {
        return metaDataLocator.findMeta(MetaDataConstants.WHITELIST_ONLY_PAGE, canonicalizedPageName, boolean.class)
                && !clientWhitelist.isClientRequestOnWhitelist();
    }
}
