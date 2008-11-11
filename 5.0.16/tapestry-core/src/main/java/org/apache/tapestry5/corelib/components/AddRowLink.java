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
 * Used inside an {@link org.apache.tapestry5.corelib.components.AjaxFormLoop} component to spur the addition of a new
 * row.  Triggers a server-side {@linkplain org.apache.tapestry5.EventConstants#ADD_ROW addRow} event on the
 * AjaxFormLoop, which must return the newly added object, which will be rendered in the body of the AjaxFormLoop and
 * sent to the client web browser.
 */
@SupportsInformalParameters
public class AddRowLink
{
    @Environmental
    private AjaxFormLoopContext context;

    @Inject
    private RenderSupport renderSupport;

    @Inject
    private ComponentResources resources;

    void beginRender(MarkupWriter writer)
    {
        String id = renderSupport.allocateClientId(resources);

        writer.element("a",
                       "id", id,
                       "href", "#");
        resources.renderInformalParameters(writer);

        context.addAddRowTrigger(id);
    }

    void afterRender(MarkupWriter writer)
    {
        writer.end();
    }
}
