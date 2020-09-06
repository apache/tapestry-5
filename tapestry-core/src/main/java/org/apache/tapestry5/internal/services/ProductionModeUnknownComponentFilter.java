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

import org.apache.tapestry5.beanmodel.services.*;
import org.apache.tapestry5.commons.util.UnknownValueException;
import org.apache.tapestry5.http.services.Request;
import org.apache.tapestry5.internal.InternalConstants;
import org.apache.tapestry5.internal.structure.Page;
import org.apache.tapestry5.services.ComponentEventRequestParameters;
import org.apache.tapestry5.services.ComponentRequestFilter;
import org.apache.tapestry5.services.ComponentRequestHandler;
import org.apache.tapestry5.services.PageRenderRequestParameters;

import java.io.IOException;

/**
 * A filter, used only in production mode, that does a "pre-flight check" that the indicated component
 * actually exists. If it does not, then the handling of the component event is aborted and other
 * hooks will ensure that request ultimately becomes a 404.
 *
 * @since 5.4
 */
public class ProductionModeUnknownComponentFilter implements ComponentRequestFilter
{
    private final Request request;

    private final RequestPageCache cache;

    public ProductionModeUnknownComponentFilter(Request request, RequestPageCache cache)
    {
        this.request = request;
        this.cache = cache;
    }

    @Override
    public void handleComponentEvent(ComponentEventRequestParameters parameters, ComponentRequestHandler handler) throws IOException
    {
        Page containerPage = cache.get(parameters.getContainingPageName());

        try
        {
            containerPage.getComponentElementByNestedId(parameters.getNestedComponentId());

            handler.handleComponentEvent(parameters);

        } catch (UnknownValueException ex)
        {
            request.setAttribute(InternalConstants.REFERENCED_COMPONENT_NOT_FOUND, true);
        }
    }

    @Override
    public void handlePageRender(PageRenderRequestParameters parameters, ComponentRequestHandler handler) throws IOException
    {
        // Pass these through to the default handler.
        handler.handlePageRender(parameters);
    }
}
