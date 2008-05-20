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

package org.apache.tapestry.corelib.base;

import org.apache.tapestry.BindingConstants;
import org.apache.tapestry.Link;
import org.apache.tapestry.MarkupWriter;
import org.apache.tapestry.annotation.Environmental;
import org.apache.tapestry.annotation.Parameter;
import org.apache.tapestry.internal.services.ClientBehaviorSupport;

import java.util.List;

/**
 * Base class for link-generating components that are based on a component event request. Such events have an event
 * context and may also update a {@link org.apache.tapestry.corelib.components.Zone}.
 */
public abstract class AbstractComponentEventLink extends AbstractLink
{
    /**
     * The context for the link (optional parameter). This list of values will be converted into strings and included in
     * the URI. The strings will be coerced back to whatever their values are and made available to event handler
     * methods.
     */
    @Parameter
    private List<?> context;

    /**
     * Binding the zone parameter turns the link into a an Ajax control that causes the related zone to be updated.
     */
    @Parameter(defaultPrefix = BindingConstants.LITERAL)
    private String zone;

    @Environmental
    private ClientBehaviorSupport clientBehaviorSupport;

    void beginRender(MarkupWriter writer)
    {
        if (isDisabled()) return;

        Object[] contextArray = context == null ? new Object[0] : context.toArray();

        Link link = createLink(contextArray);

        writeLink(writer, link);

        if (zone != null) clientBehaviorSupport.linkZone(getClientId(), zone);
    }

    /**
     * Invoked to create the Link that will become the href attribute of the output.
     */
    protected abstract Link createLink(Object[] eventContext);

    void afterRender(MarkupWriter writer)
    {
        if (isDisabled()) return;

        writer.end(); // <a>
    }

}
