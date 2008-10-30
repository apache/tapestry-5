// Copyright 2008 The Apache Software Foundation
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

package org.apache.tapestry5.corelib.components;

import org.apache.tapestry5.ComponentResources;
import org.apache.tapestry5.MarkupWriter;
import org.apache.tapestry5.RenderSupport;
import org.apache.tapestry5.annotations.Environmental;
import org.apache.tapestry5.annotations.SupportsInformalParameters;
import org.apache.tapestry5.corelib.internal.AjaxFormLoopContext;
import org.apache.tapestry5.ioc.annotations.Inject;

/**
 * Used inside a {@link org.apache.tapestry5.corelib.components.AjaxFormLoop} to remove the current row from the loop.
 * This fires a server-side  event (from the AjaxFormLoop component); the event context is the object to be removed. On
 * the client-side, the element for the row is hidden, then removed altogether.
 */
@SupportsInformalParameters
public class RemoveRowLink
{
    @Inject
    private ComponentResources resources;

    @Environmental
    private AjaxFormLoopContext context;

    @Environmental
    private RenderSupport renderSupport;

    void beginRender(MarkupWriter writer)
    {
        String clientId = renderSupport.allocateClientId(resources);

        writer.element("a",

                       "href", "#",

                       "id", clientId);

        resources.renderInformalParameters(writer);

        context.addRemoveRowTrigger(clientId);
    }


    void afterRender(MarkupWriter writer)
    {
        writer.end();
    }
}
