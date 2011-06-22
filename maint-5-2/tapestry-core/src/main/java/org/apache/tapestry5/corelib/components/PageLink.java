// Copyright 2007, 2008 The Apache Software Foundation
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

import org.apache.tapestry5.BindingConstants;
import org.apache.tapestry5.ComponentResources;
import org.apache.tapestry5.Link;
import org.apache.tapestry5.MarkupWriter;
import org.apache.tapestry5.annotations.Parameter;
import org.apache.tapestry5.corelib.base.AbstractLink;
import org.apache.tapestry5.ioc.annotations.Inject;

/**
 * Generates a render request link to some other page in the application. If an activation context is supplied (as the
 * context parameter), then the context values will be encoded into the URL. If no context is supplied, then the target
 * page itself will supply the context via a passivate event.
 * <p/>
 * Pages are not required to have an activation context. When a page does have an activation context, the value
 * typically represents the identity of some object displayed or otherwise manipulated by the page.
 */
public class PageLink extends AbstractLink
{
    /**
     * The logical name of the page to link to.
     */
    @Parameter(required = true, allowNull = false, defaultPrefix = BindingConstants.LITERAL)
    private String page;

    @Inject
    private ComponentResources resources;

    /**
     * If provided, this is the activation context for the target page (the information will be encoded into the URL).
     * If not provided, then the target page will provide its own activation context.
     */
    @Parameter
    private Object[] context;

    void beginRender(MarkupWriter writer)
    {
        if (isDisabled()) return;

        Link link = resources.createPageLink(page, resources.isBound("context"), context);

        writeLink(writer, link);
    }

    void afterRender(MarkupWriter writer)
    {
        if (isDisabled()) return;

        writer.end(); // <a>
    }
}
